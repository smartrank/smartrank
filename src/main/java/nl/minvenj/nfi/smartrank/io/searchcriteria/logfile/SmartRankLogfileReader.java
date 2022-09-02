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
package nl.minvenj.nfi.smartrank.io.searchcriteria.logfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.samples.SampleReader;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;

/**
 * Reads search criteria from a SmartRank logfile.
 */
public class SmartRankLogfileReader implements SearchCriteriaReader {

    private final List<Sample> _samples;
    private final List<Sample> _profiles;
    private final Map<String, Double> _hpContributors;
    private final UnknownDonorDefinition _hdUnknowns;
    private final UnknownDonorDefinition _hpUnknowns;
    private final Map<String, Double> _hdContributors;
    private final String _resultLocation;
    private Double _candidateDropout;
    private Double _theta;
    private Double _dropin;
    private Integer _lrThreshold;
    private Double _rareAlleleFrequency;
    private Date _dateTime;

    /**
     * A helper class to store the number and dropout probability of unknown contributors.
     */
    private class UnknownDonorDefinition {
        public int _count = 0;
        public Double _dropout = 0.0;
    }

    public SmartRankLogfileReader(final String fileName, final String criteria) throws IOException {
        _samples = new ArrayList<>();
        _profiles = new ArrayList<>();
        _hpContributors = new HashMap<>();
        _hdContributors = new HashMap<>();
        _hdUnknowns = new UnknownDonorDefinition();
        _hpUnknowns = new UnknownDonorDefinition();
        _resultLocation = "";
        _candidateDropout = 0.0;

        readFile(new BufferedReader(new StringReader(criteria)));
    }

    private void readFile(final BufferedReader reader) throws IOException {
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                try {
                    _dateTime = DateFormat.getInstance().parse(line);
                }
                catch (final ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                first = false;
            }

            if (line.startsWith("Loaded replicates:")) {
                loadSamples(reader, _samples);
            }
            if (line.startsWith("Loaded profiles:")) {
                loadSamples(reader, _profiles);
            }
            if (line.startsWith("Hypothesis Prosecution")) {
                _candidateDropout = loadHypothesis(reader, _hpContributors, _hpUnknowns);
            }
            if (line.startsWith("Hypothesis Defense")) {
                loadHypothesis(reader, _hdContributors, _hdUnknowns);
            }
        }
    }

    private Double loadHypothesis(final BufferedReader reader, final Map<String, Double> contributors, final UnknownDonorDefinition unknowns) throws IOException {
        Double candidateDropout = 0.0;

        final Pattern p = Pattern.compile("([^\\(]+)\\(([^\\)]+)\\)\\,?\\s?");

        String line;
        while (((line = reader.readLine()) != null) && !line.startsWith("=================")) {
            line = line.trim();
            if (line.startsWith("Contributors")) {
                final Matcher matcher = p.matcher(line.subSequence(14, line.length() - 1));
                while (matcher.find()) {
                    final String name = matcher.group(1);
                    final String dropout = matcher.group(2);
                    if (name.equalsIgnoreCase("Candidate")) {
                        candidateDropout = Double.valueOf(dropout);
                    }
                    else {
                        contributors.put(name, Double.valueOf(dropout));
                    }
                }
            }

            if (line.startsWith("Unknowns")) {
                unknowns._count = Integer.parseInt(line.substring(8).trim());
            }

            if (line.startsWith("Unknown Dropout")) {
                unknowns._dropout = Double.parseDouble(line.substring(16).trim());
            }

            if (line.startsWith("Dropin ")) {
                _dropin = Double.parseDouble(line.substring(7));
            }

            if (line.startsWith("Theta ")) {
                _dropin = Double.parseDouble(line.substring(6));
            }

            if (line.startsWith("LR Threshold: ")) {
                _lrThreshold = Integer.parseInt(line.substring(14));
            }

            if (line.startsWith("Rare Allele Frequency: ")) {
                _rareAlleleFrequency = Double.parseDouble(line.substring(14));
            }
        }

        return candidateDropout;
    }

    private void loadSamples(final BufferedReader reader, final Collection<Sample> destination) throws IOException {
        final Pattern p = Pattern.compile("^\\s*(.+) loaded from \\'(.*)\\' file hash (.*)$");
        final HashMap<String, Collection<Sample>> sampleCollections = new HashMap<>();
        String line;
        while (((line = reader.readLine()) != null) && !line.startsWith("=================")) {
            final Matcher matcher = p.matcher(line);
            if (matcher.matches()) {
                final String sampleName = matcher.group(1);
                final String fileName = matcher.group(2);
                final String fileHash = matcher.group(3);

                Collection<Sample> samples = sampleCollections.get(fileName);
                if (samples == null) {
                    final SampleReader samplereader = new SampleReader(new File(fileName));
                    if (!samplereader.getFileHash().equalsIgnoreCase(fileHash)) {
                        throw new IllegalArgumentException("Expected hash " + fileHash + " but got " + samplereader.getFileHash() + " for file " + fileName);
                    }
                    samples = samplereader.getSamples();
                    sampleCollections.put(fileName, samples);
                }

                for (final Sample sample : samples) {
                    if (sample.getName().equalsIgnoreCase(sampleName)) {
                        destination.add(sample);
                    }
                }
            }
        }
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
    public Double getTheta() {
        return _theta;
    }

    @Override
    public Double getDropin() {
        return _dropin;
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
        return false;
    }

    @Override
    public int getMaximumNumberOfResults() {
        return -1;
    }

    @Override
    public PopulationStatistics getPopulationStatistics() {
        return null;
    }

    @Override
    public String getRequester() {
        return "";
    }

    @Override
    public Date getRequestDateTime() {
        return _dateTime;
    }

    @Override
    public Properties getProperties() {
        return new Properties();
    }
}
