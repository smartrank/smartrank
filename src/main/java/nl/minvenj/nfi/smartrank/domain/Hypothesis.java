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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.raven.numberformat.NumberUtils;

public abstract class Hypothesis {

    private static final Logger LOG = LoggerFactory.getLogger(Hypothesis.class);
    public static final Sample DEFAULT_SAMPLE = new Sample("Candidate", "");

    ArrayList<Contributor> _contributors = new ArrayList<>();
    ArrayList<Contributor> _nonContributors = new ArrayList<>();
    ArrayList<Sample> _allSamples = new ArrayList<>();
    int _unknownContributors = 0;
    double _dropInProbability = 0;
    private final String _id;
    PopulationStatistics _populationStatistics;
    double _thetaCorrection;
    double _unknownDropoutProbability;
    private boolean _shutDown;

    public Hypothesis(final String id) {
        _id = id;
        _thetaCorrection = 0;
    }

    public Contributor addContributor(final Sample contributorSample, final double dropOutProbability) {
        LOG.debug("addContributor {} ({})", contributorSample.getName(), dropOutProbability);
        Contributor contributor = null;
        if (!_allSamples.contains(contributorSample)) {
            contributor = new Contributor(contributorSample, dropOutProbability);
            _allSamples.add(contributorSample);
            _contributors.add(contributor);
        } else {
            contributor = getContributor(contributorSample);
            contributor.setDropoutProbability(dropOutProbability);
            if (_nonContributors.remove(contributor)) {
                _contributors.add(contributor);
            }
        }
        return contributor;
    }

    public void addNonContributor(final Sample contributorSample, final double dropOutProbability) {
        if (!_allSamples.contains(contributorSample)) {
            _allSamples.add(contributorSample);
            _nonContributors.add(new Contributor(contributorSample, dropOutProbability));
        } else {
            final Contributor c = getContributor(contributorSample);
            c.setDropoutProbability(dropOutProbability);
            if (_contributors.remove(c)) {
                _nonContributors.add(c);
            }
        }
    }

    public int getUnknownCount() {
        return _unknownContributors;
    }

    public void setUnknownCount(final int unknowns) {
        this._unknownContributors = unknowns;
    }

    public String getId() {
        return _id;
    }

    public Collection<Contributor> getContributors() {
        return _contributors;
    }

    public Collection<Contributor> getNonContributors() {
        return Collections.unmodifiableCollection(_nonContributors);
    }

    public void removeContributor(final Sample sample) {
        _allSamples.remove(sample);

        for (final Contributor con : _contributors) {
            if (con.getSample() == sample) {
                _contributors.remove(con);
                return;
            }
        }

        for (final Contributor con : _nonContributors) {
            if (con.getSample() == sample) {
                _nonContributors.remove(con);
                return;
            }
        }
    }

    public Collection<Sample> getSamples() {
        return Collections.unmodifiableCollection(_allSamples);
    }

    /**
     * @return the populationStatistics
     */
    public PopulationStatistics getPopulationStatistics() {
        if (_populationStatistics == null) {
            throw new IllegalStateException("Population Statistics not loaded!");
        }
        return _populationStatistics;
    }

    /**
     * @return the dropInProbability
     */
    public double getDropInProbability() {
        return _dropInProbability;
    }

    public void setDropInProbability(final double dropIn) {
        this._dropInProbability = dropIn;
    }

    public Contributor getContributor(final Allele a) {
        for (final Contributor contributor : _contributors) {
            if (contributor.getSample().getName().equals(a.getLocus().getSample().getName())) {
                return contributor;
            }
        }
        throw new IllegalArgumentException("Contributor for Allele '" + a.getAllele() + "' (" + a.getLocus().getSample().getName() + ") not found in hypothesis '" + getId() + "'!");
    }

    public double getThetaCorrection() {
        return _thetaCorrection;
    }

    public void setThetaCorrection(final double theta) {
        this._thetaCorrection = theta;
    }

    public double getUnknownDropoutProbability() {
        return _unknownDropoutProbability;
    }

    public void setUnknownDropoutProbability(final double dropOut) {
        this._unknownDropoutProbability = dropOut;
    }

    public Contributor getContributor(final Sample sample) {
        return getContributor(sample.getName());
    }

    public Contributor getContributor(final String id) {
        for (final Contributor contributor : _contributors) {
            if (contributor.getSample().getName().equalsIgnoreCase(id)) {
                return contributor;
            }
        }
        for (final Contributor contributor : _nonContributors) {
            if (contributor.getSample().getName().equalsIgnoreCase(id)) {
                return contributor;
            }
        }
        throw new IllegalArgumentException("Sample '" + id + "' is not present in hypothesis '" + getId() + "'!");
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (!_contributors.isEmpty()) {
            for (final Contributor contributor : _contributors) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(contributor.toString());
            }
        }

        if (this.getUnknownCount() > 0) {
            if (builder.length() > 0) {
                builder.append(" and ");
            }
            final BigDecimal udp = new BigDecimal(_unknownDropoutProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
            builder.append(getUnknownCount()).append(getUnknownCount() != 1 ? " unknowns (" : " unknown (").append(udp).append(")");
        }

        final BigDecimal di = new BigDecimal(_dropInProbability, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);
        final BigDecimal tc = new BigDecimal(_thetaCorrection, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP);

        builder.append(", DropIn ").append(di).append(", Theta ").append(tc);

        return builder.toString();
    }

    public String getGuid() {
        return toString() + "/" + (_populationStatistics == null ? "null" : _populationStatistics.getFileHash() + "/" + NumberUtils.format(4, _populationStatistics.getRareAlleleFrequency()));
    }

    /**
     * Investigates whether the given sample is marked as a contributor for this
     * hypothesis
     *
     * @param sample The sample to search for
     *
     * @return true if the sample is marked as a contributor
     */
    public boolean isContributor(final Sample sample) {
        for (final Contributor contributor : _contributors) {
            if (contributor.getSample().getName().equalsIgnoreCase(sample.getName()) && contributor.getSample().getSourceFile().equalsIgnoreCase(sample.getSourceFile())) {
                return true;
            }
        }
        return false;
    }

    public void setStatistics(final PopulationStatistics stats) {
        _populationStatistics = stats;
    }

    /**
     * Sets the candidate sample for this hypothesis.
     *
     * @param candidateSample The sample to use as candidate.
     */
    public abstract void setCandidate(Sample candidateSample);

    /**
     * Copies a hypothesis.
     *
     * @return a copy of the current hypothesis
     */
    public abstract Hypothesis copy();

    /**
     * Investigates whether or not the hypothesis has a candidate
     *
     * @return true if the hypothesis has a candidate
     */
    public abstract Boolean hasCandidate();

    /**
     * Gets the sample used for the candidate.
     *
     * @return The candidate sample
     */
    public abstract Sample getCandidate();

    /**
     * Indicates if the Q designation is shut down for this hypothesis. If the Q designation is shut down,
     * any unknown contributors will will be composed of the the alleles present in the sample.
     * If the Q designation is not shut down, the alleles for unknowns are sourced from those recorded in the frequency table.
     *
     * @return <b>true</b> if the Q Designation for the current hypothesis is shut down
     */
    public boolean isQDesignationShutdown() {
        return _shutDown;
    }

    public void setQDesignationShutdown(final boolean shutDown) {
        _shutDown = shutDown;
    }
}
