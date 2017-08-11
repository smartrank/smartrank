/*
 * Copyright (C) 2016 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.io.searchcriteria.locim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.HashingReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.AlleleStatisticsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.AlleleType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.ContributorType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.LocusStatisticsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.LocusType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.ReplicateType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.SmartRankImportFile;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.SpecimenType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.StatisticsType;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

/**
 * Reads search criteria from a Smartrank Import File.
 */
public class SmartRankImportFileReader implements SearchCriteriaReader {

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankImportFileReader.class);

    private final ArrayList<Sample> _samples;
    private final ArrayList<Sample> _profiles;
    private final HashMap<String, Double> _hpContributors;
    private final HashMap<String, Double> _hdContributors;
    private final UnknownDonorDefinition _hdUnknowns;
    private final UnknownDonorDefinition _hpUnknowns;
    private final String _fileName;
    private String _resultLocation;
    private double _candidateDropout;
    private double _dropin;
    private double _theta;
    private int _lrThreshold;
    private double _rareAlleleFrequency;
    private Boolean _performParameterEstimation;
    private int _maxReturnedResults;
    private PopulationStatistics _statistics;
    private String _userId;
    private Date _dateTime;

    /**
     * A helper class to store the number and dropout probability of unknown contributors.
     */
    private class UnknownDonorDefinition {
        public int _count = 0;
        public Double _dropout = 0.0;
    }

    public SmartRankImportFileReader(final File file) throws IOException {
        _samples = new ArrayList<>();
        _profiles = new ArrayList<>();
        _hpContributors = new HashMap<>();
        _hdContributors = new HashMap<>();
        _hdUnknowns = new UnknownDonorDefinition();
        _hpUnknowns = new UnknownDonorDefinition();
        _resultLocation = "";
        _candidateDropout = 0.0;
        _fileName = file.getAbsolutePath();

        try (HashingReader reader = new HashingReader(new BufferedReader(new FileReader(file)))) {
            readFile(reader);
            setHashes(reader.getHash());
        }
    }

    private void readFile(final Reader reader) {
        final HashMap<String, Sample> specimens = new HashMap<>();
        final SmartRankImportFile importFile = JAXB.unmarshal(reader, SmartRankImportFile.class);

        _userId = importFile.getUserid();
        if (_userId == null) {
            try {
                _userId = Files.getOwner(new File(_fileName).toPath(), LinkOption.NOFOLLOW_LINKS).getName();
            }
            catch (final Exception e) {
                LOG.info("Could not determine owner of {}: {} - {}", _fileName, e.getClass().getSimpleName(), e.getMessage());
            }
        }

        String dateTime = NullUtils.getValue(importFile.getDateTime(), "");
        if (!dateTime.isEmpty()) {
            // This to cater for Excel-exported dates, which have improperly formatted datetime values (yyyy-MM-dd HH:mm:ss instead of the specified yyyy-MM-ddTHH:mm:ss)
            dateTime = dateTime.replaceFirst("(\\d{4}-\\d{2}-\\d{2}) (\\d\\d:\\d\\d:\\d\\d)", "$1T$2");
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                _dateTime = sdf.parse(dateTime);
            }
            catch (final ParseException e) {
                LOG.warn("File contained an unrecognized date:'{}'. expected yyyy-MM-ddTHH:mm:ss", dateTime);
            }
        }
        else {
            try {
                final BasicFileAttributes attributes = Files.readAttributes(new File(_fileName).toPath(), BasicFileAttributes.class);
                _dateTime = new Date(attributes.creationTime().toMillis());
            }
            catch (final IOException e) {
                LOG.info("Could not determine creating time of {}: {}", _fileName, e.getMessage());
            }
        }

        for (final SpecimenType specimen : importFile.getSpecimen()) {
            final Sample sample = readSpecimen(specimen);
            specimens.put(sample.getName(), sample);
        }

        for (final ReplicateType replicate : importFile.getReplicates().getReplicate()) {
            final Sample sample = specimens.get(replicate.getName());
            if (sample == null) {
                throw new IllegalArgumentException("Replicate '" + replicate.getName() + "' was defined as replicate but the sample was not present in the file.");
            }
            _samples.add(sample);
        }

        if (importFile.getHD().getContributors() != null) {
            for (final ContributorType contributorHd : importFile.getHD().getContributors().getContributor()) {
                _hdContributors.put(contributorHd.getName(), getAndCheckDropout("Hd", contributorHd));
                final Sample sample = specimens.get(contributorHd.getName());
                if (sample == null) {
                    throw new IllegalArgumentException("'" + contributorHd.getName() + "' is contributor under Hd but no specimen of that name is present in the file!");
                }
                if (!_profiles.contains(sample)) {
                    _profiles.add(sample);
                }
            }
        }

        _hdUnknowns._count = importFile.getHD().getUnknowns().getCount();
        _hdUnknowns._dropout = getAndCheckDropout("Hd", "unknown contributors", importFile.getHD().getUnknowns().getDropout());

        if (importFile.getHP().getContributors() != null) {
            for (final ContributorType contributorHp : importFile.getHP().getContributors().getContributor()) {
                _hpContributors.put(contributorHp.getName(), getAndCheckDropout("Hp", contributorHp));
                final Sample sample = specimens.get(contributorHp.getName());
                if (sample == null) {
                    throw new IllegalArgumentException("'" + contributorHp.getName() + "' is contributor under Hp but no specimen of that name is present in the file!");
                }
                if (!_profiles.contains(sample)) {
                    _profiles.add(sample);
                }
            }
        }

        _hpUnknowns._count = importFile.getHP().getUnknowns().getCount();
        _hpUnknowns._dropout = getAndCheckDropout("Hp", "unknown contributors", importFile.getHP().getUnknowns().getDropout());

        _candidateDropout = getAndCheckDropout("Hp", "Candidate", importFile.getHP().getCandidateDropout());
        _resultLocation = importFile.getCaseFolder();

        _rareAlleleFrequency = importFile.getRareAlleleFrequency();
        _lrThreshold = importFile.getLrThreshold();
        _maxReturnedResults = Integer.parseInt(NullUtils.getValue(importFile.getMaximumNumberOfResults(), "-1"));

        if (importFile.getDropin() != null)
            _dropin = importFile.getDropin().doubleValue();

        if (importFile.getTheta() != null)
            _theta = importFile.getTheta().doubleValue();

        if (importFile.getStatistics() != null) {
            readStatistics(importFile);
        }
    }

    private void setHashes(final String hash) {
        for (final Sample sample : _samples) {
            sample.setSourceFileHash(hash);
        }

        if (_statistics != null) {
            _statistics.setFileHash(hash);
        }
    }

    private void readStatistics(final SmartRankImportFile importFile) {
        final StatisticsType statsType = importFile.getStatistics();
        if(statsType!=null) {
            final Double rare = statsType.getRareAlleleFrequency();
            if (rare != null) {
                _rareAlleleFrequency = rare;
            }

            final PopulationStatistics stats = new PopulationStatistics(_fileName);
            stats.setRareAlleleFrequency(_rareAlleleFrequency);

            for (final LocusStatisticsType locusStats : statsType.getLocus()) {
                for (final AlleleStatisticsType alleleStats : locusStats.getAllele()) {
                    stats.addStatistic(locusStats.getName(), alleleStats.getValue(), new BigDecimal(alleleStats.getProbability()));
                }
            }
            _statistics = stats;
        }
    }

    private double getAndCheckDropout(final String hypo, final String contributorName, final String value) {
        if (value.equalsIgnoreCase("automatic")) {
            if (_performParameterEstimation != null && !_performParameterEstimation) {
                throw new IllegalArgumentException("Parameter Estimation is not 'automatic' but under " + hypo + " '" + contributorName + "' had a dropout set to automatic! When automatic estimation is enabled, it must apply to all known and unknown contributors!");
            }
            if (_performParameterEstimation == null) {
                _performParameterEstimation = new Boolean(true);
            }
            _performParameterEstimation = true;
            return 0.0;
        }

        if (_performParameterEstimation != null && _performParameterEstimation) {
            throw new IllegalArgumentException("Parameter Estimation is set to 'automatic' but under " + hypo + " '" + contributorName + "' had a dropout set to a fixed value! When automatic estimation is enabled, it must apply to all known and unknown contributors!");
        }

        if (_performParameterEstimation == null) {
            _performParameterEstimation = new Boolean(false);
        }
        return Double.parseDouble(value);
    }

    private double getAndCheckDropout(final String hypoName, final ContributorType contributor) {
        return getAndCheckDropout(hypoName, contributor.getName(), contributor.getDropout());
    }

    private Sample readSpecimen(final SpecimenType specimen) {
        final Sample sample = new Sample(specimen.getName(), _fileName);

        for (final LocusType locus : specimen.getLocus()) {

            final String locusName = locus.getName();
            final Locus newLoc = new Locus(locusName);
            for (final AlleleType allele : locus.getAllele()) {
                newLoc.addAllele(new Allele(allele.getValue()));
            }
            sample.addLocus(newLoc);
        }
        return sample;
    }

    @Override
    public List<Sample> getCrimesceneSamples() {
        return _samples;
    }

    @Override
    public List<Sample> getKnownProfiles() {
        return _profiles;
    }

    @Override
    public Map<String, Double> getHpContributors() {
        return _hpContributors;
    }

    @Override
    public int getHpUnknowns() {
        return _hpUnknowns._count;
    }

    @Override
    public Double getHpUnknownDropout() {
        return _hpUnknowns._dropout;
    }

    @Override
    public Map<String, Double> getHdContributors() {
        return _hdContributors;
    }

    @Override
    public int getHdUnknowns() {
        return _hdUnknowns._count;
    }

    @Override
    public Double getHdUnknownDropout() {
        return _hdUnknowns._dropout;
    }

    @Override
    public String getResultLocation() {
        return _resultLocation;
    }

    @Override
    public Double getCandidateDropout() {
        return _candidateDropout;
    }

    @Override
    public Double getDropin() {
        return _dropin;
    }

    @Override
    public Double getTheta() {
        return _theta;
    }

    @Override
    public Integer getLRThreshold() {
        return _lrThreshold;
    }

    @Override
    public Double getRareAlleleFrequency() {
        return _rareAlleleFrequency;
    }

    @Override
    public boolean isAutomaticParameterEstimationToBePerformed() {
        return _performParameterEstimation == null ? false : _performParameterEstimation;
    }

    @Override
    public int getMaximumNumberOfResults() {
        return _maxReturnedResults;
    }

    @Override
    public PopulationStatistics getPopulationStatistics() {
        return _statistics;
    }

    @Override
    public String getRequester() {
        return _userId;
    }

    @Override
    public Date getRequestDateTime() {
        return _dateTime;
    }
}
