package nl.minvenj.nfi.smartrank.io.databases.codis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import nl.minvenj.nfi.smartrank.analysis.ExclusionReason;
import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.CSVReader;

/**
 * Reads a single record from a CSV reader and decodes the fields into a Sample object.
 */
public class CodisRecordReader implements Callable<CodisRecord> {

    private final String[] _headers;
    private final CSVReader _reader;
    private final Collection<String> _enabledLoci;
    private final int _minimumNumberOfLoci;
    private final boolean _dropoutAllowed;
    private final Collection<Sample> _crimesceneProfiles;

    public CodisRecordReader(final CSVReader reader, final String[] headers, final Collection<String> enabledLoci, final Collection<Sample> crimesceneProfiles, final int minimumNumberOfLoci, final boolean dropoutAllowed) {
        _reader = reader;
        _headers = headers;
        _enabledLoci = enabledLoci;
        _crimesceneProfiles = crimesceneProfiles;
        _minimumNumberOfLoci = minimumNumberOfLoci;
        _dropoutAllowed = dropoutAllowed;
    }

    private CodisRecord buildSample(final String[] fields) {
        Sample sample = null;
        sample = new Sample(fields[0].trim());

        try {
            if (fields.length != _headers.length) {
                throw new IndexOutOfBoundsException();
            }

            int idx = 1;
            while (idx < fields.length) {
                final String allele = fields[idx].trim();
                if (!allele.equalsIgnoreCase("NULL")) {
                    final String locusName = _headers[idx];
                    Locus locus = sample.getLocus(locusName);
                    if (locus == null) {
                        locus = new Locus(locusName);
                        sample.addLocus(locus);
                    }
                    if (allele.matches(CodisRecordValidator.VALID_ALLELE_REGEX)) {
                        locus.addAllele(new Allele(allele));
                    }
                }
                idx++;
            }

            // Perform a sanity check on the loci in the sample.
            final Collection<Locus> loci = new ArrayList<>(sample.getLoci()); // Note: Wrap in a new ArrayList to allow the remove to work
            for (final Locus locus : loci) {
                // Empty loci and loci with more than 2 alleles are removed
                if (locus.size() == 0 || locus.size() > 2) {
                    sample.removeLocus(locus);
                }
                // Loci with one allele are assumed to be homozygotic
                if (locus.size() == 1) {
                    locus.addAllele(locus.getAlleles().iterator().next());
                }
            }

            // Check that the specimen has sufficient overlap with the sample
            if (getEnabledLocusCount(sample) < _minimumNumberOfLoci) {
                return new CodisRecord(sample, ExclusionReason.NOT_ENOUGH_LOCI);
            }

            // Check that the specimen does not need dropout to explain the evidence if PrD is set to 0
            if (!_dropoutAllowed && needsDropout(sample)) {
                return new CodisRecord(sample, ExclusionReason.REQUIRES_DROPOUT);
            }
            return new CodisRecord(sample);
        }
        catch (final IndexOutOfBoundsException iobe) {
            return new CodisRecord(sample, ExclusionReason.UNEXPECTED_NUMBER_OF_FIELDS);
        }
        catch (final Throwable t) {
            t.printStackTrace();
            return new CodisRecord(sample, ExclusionReason.OTHER);
        }
    }

    private boolean needsDropout(final Sample reference) {
        for (final String locus : _enabledLoci) {
            final Locus referenceLocus = reference.getLocus(locus);
            if (referenceLocus != null && needsDropout(referenceLocus)) {
                return true;
            }
        }
        return false;
    }

    private boolean needsDropout(final Locus locus) {
        for (final Sample stain : _crimesceneProfiles) {
            final Locus stainLocus = stain.getLocus(locus.getName());
            if (stainLocus != null) {
                for (final Allele refAllele : locus.getAlleles()) {
                    if (!stainLocus.hasAllele(refAllele.getAllele()))
                        return true;
                }
            }
        }
        return false;
    }

    private int getEnabledLocusCount(final Sample profile) {
        int count = 0;
        for (final String locus : _enabledLoci) {
            if (profile.hasLocus(locus)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public CodisRecord call() {
        try {
            final String[] fields = _reader.readFields();
            if (fields == null) {
                return CodisRecord.END_OF_FILE;
            }
            return buildSample(fields);
        }
        catch (final Throwable t) {
            return new CodisRecord(null, ExclusionReason.OTHER);
        }
    }
}
