/*
 * Copyright (C) 2015 Netherlands Forensic Institute
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

package nl.minvenj.nfi.smartrank.domain;

import java.util.ArrayList;

import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;

public class DefenseHypothesis extends Hypothesis {

    public DefenseHypothesis() {
        super("Defense");
        setUnknownDropoutProbability(SmartRankRestrictions.getDropoutDefault());
        setQDesignationShutdown(false);
    }

    @Override
    public void setCandidate(final Sample candidateSample) {
    }

    @Override
    public Sample getCandidate() {
        return null;
    }

    @Override
    public Boolean hasCandidate() {
        return false;
    }

    public void reset() {
    }

    @Override
    public Hypothesis copy() {
        final Hypothesis retval = new DefenseHypothesis();
        retval._unknownContributors = _unknownContributors;
        retval._populationStatistics = _populationStatistics;
        retval._dropInProbability = _dropInProbability;
        retval._unknownDropoutProbability = _unknownDropoutProbability;
        retval._thetaCorrection = _thetaCorrection;
        retval._allSamples = new ArrayList<>(_allSamples);
        retval._contributors = new ArrayList<>();
        for (final Contributor contributor : _contributors) {
            retval._contributors.add(new Contributor(contributor));
        }

        retval._nonContributors = new ArrayList<>();
        for (final Contributor contributor : _nonContributors) {
            retval._nonContributors.add(new Contributor(contributor));
        }
        retval.setQDesignationShutdown(isQDesignationShutdown());
        return retval;
    }
}
