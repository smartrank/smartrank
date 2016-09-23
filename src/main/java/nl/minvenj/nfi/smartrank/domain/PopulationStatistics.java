/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class PopulationStatistics {

    // The default probability for a rare allele is 1 / (2 * N)
    // where N is the number of individuals that were sampled in the population
    // study carried out to estimate the allele frequencies.
    private static final int DEFAULT_POPULATION_STUDY_SIZE = 2085;
    private static final double DEFAULT_FREQUENCY = 1.0 / (2 * DEFAULT_POPULATION_STUDY_SIZE);

    private final ArrayList<String> _loci;
    private final ArrayList<String> _allelesForLocus;
    private final String _fileName;

    private String _fileHash;
    private final double[][] _probs = new double[50][1024];
    private final boolean[][] _rare = new boolean[50][1024];
    private double _rareAlleleFrequency = DEFAULT_FREQUENCY;

    public PopulationStatistics(final String fileNamez) {
        _loci = new ArrayList<>();
        _allelesForLocus = new ArrayList<>();
        for (int idx = 0; idx < _rare.length; idx++) {
            Arrays.fill(_rare[idx], true);
        }
        _fileName = fileNamez;
    }

    /**
     * @return The frequency of rare alleles (i.e. alleles that are not recorded
     * in the population statistics file)
     */
    public Double getRareAlleleFrequency() {
        return _rareAlleleFrequency;
    }

    /**
     * Adds a statistic entry (i.e. the observed frequency of an allele at a
     * certain locus) to the population statistics.
     *
     * @param locusName The name of the locus
     * @param alleleName The name of the allele
     * @param probability The probability that the allele is observed at the
     * locus
     */
    public void addStatistic(final String locusName, final String alleleName, final BigDecimal probability) {
        final String normalizedAllele = Allele.normalize(alleleName);
        final String normalizedLocusName = Locus.normalize(locusName);
        if (!_loci.contains(normalizedLocusName)) {
            _loci.add(normalizedLocusName);
        }
        final int locusId = Locus.getId(normalizedLocusName);
        final int alleleId = Allele.getId(normalizedAllele);
        _probs[locusId][alleleId] = probability.doubleValue();
        _rare[locusId][alleleId] = false;
        _allelesForLocus.add(normalizedLocusName + normalizedAllele);
    }

    private Double getProbability(final int locusId, final int alleleId) {
        if (_rare[locusId][alleleId]) {
            return _rareAlleleFrequency;
        }
        return _probs[locusId][alleleId];
    }

    public Double getProbability(final Locus locus, final Allele allele) {
        return getProbability(locus.getId(), allele.getId());
    }

    public Double getProbability(final String locusId, final String allele) {
        return getProbability(Locus.getId(locusId), Allele.getId(allele));
    }

    public Collection<String> getAlleles(final String locusName) {
        final ArrayList<String> alleles = new ArrayList<>();
        final String normalizedLocusName = Locus.normalize(locusName);
        for (final String locus : _allelesForLocus) {
            if (locus.startsWith(normalizedLocusName)) {
                alleles.add(locus.substring(normalizedLocusName.length()));
            }
        }
        return alleles;
    }

    public String getFileName() {
        return _fileName;
    }

    public Collection<String> getLoci() {
        return Collections.unmodifiableCollection(_loci);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PopulationStatistics other = (PopulationStatistics) obj;
        if (!Objects.equals(this._loci, other._loci)) {
            return false;
        }
        if (!Objects.equals(this._fileName, other._fileName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this._loci);
        hash = 83 * hash + Objects.hashCode(this._fileName);
        return hash;
    }

    /**
     * Sets the file hash
     *
     * @param fileHash The hash to set
     */
    public void setFileHash(final String fileHash) {
        this._fileHash = fileHash;
    }

    /**
     * @return the fileHash
     */
    public String getFileHash() {
        return _fileHash;
    }

    @Override
    public String toString() {
        return _fileName + " " + _fileHash;
    }

    /**
     * Determines if the supplied allele is rare (i.e. is not recorded) in the
     * currently opened statistics file.
     *
     * @param allele The allele to query
     * @return true if the supplied allele is rare
     */
    public boolean isRareAllele(final Allele allele) {
        return _rare[allele.getLocus().getId()][allele.getId()];
    }

    public void setRareAlleleFrequency(final double frequency) {
        _rareAlleleFrequency = frequency;
    }
}
