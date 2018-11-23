package nl.minvenj.nfi.smartrank.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nl.minvenj.nfi.smartrank.io.databases.codis.CodisRecordValidator;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.CSVReader;

public class ReadTest {

    private static class FileParser implements Callable<Integer> {
        private final CSVReader _reader;
        private final String[] _headers;

        public FileParser(final String[] headers, final CSVReader reader) {
            _headers = headers;
            _reader = reader;

        }

        @Override
        public Integer call() throws Exception {

            String[] fields;
            int recordNumber = 0;
            while ((fields = _reader.readFields()) != null) {
                try {
                    final Sample sample = new Sample(fields[0].trim());

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
                    recordNumber++;
                }
                catch (final Throwable e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return recordNumber;
        }
    }

    public static void main(final String[] args) throws Exception {
        final CSVReader reader = new CSVReader(new File(args[0]));
        final String[] headers = reader.readFields();

        final long start = System.currentTimeMillis();
        final ExecutorService pool = Executors.newCachedThreadPool();
        final ArrayList<Future<Integer>> futures = new ArrayList<>();
        for (int count = 0; count < 4; count++) {
            futures.add(pool.submit(new FileParser(headers, reader)));
        }
        int total = 0;
        for (final Future<Integer> future : futures) {
            total += future.get();
        }
        System.out.println("Total:  " + total);
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms");
    }

}
