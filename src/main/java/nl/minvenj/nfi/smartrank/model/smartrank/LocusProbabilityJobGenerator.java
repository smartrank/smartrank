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
package nl.minvenj.nfi.smartrank.model.smartrank;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class LocusProbabilityJobGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(LocusProbabilityJobGenerator.class);

    private LocusProbabilityJobGenerator() {
    }

    public static ArrayList<LocusProbabilityJob> generate(String locusName, AnalysisParameters parameters, Locus[] possibleAlleleCombinations, Hypothesis hypothesis) {

        ArrayList<LocusProbabilityJob> retval = new ArrayList<>();
        final Collection<Sample> crimesceneProfiles = parameters.getEnabledCrimesceneProfiles();

        if (hypothesis.getUnknownCount() > 0) {
            for (int idx = 0; idx < possibleAlleleCombinations.length; idx++) {
                PermutationIterator permutationIterator = new PermutationIteratorPlain(hypothesis.getUnknownCount(), possibleAlleleCombinations, idx);
                LocusProbabilityJob job = new LocusProbabilityJob(locusName, permutationIterator, crimesceneProfiles, hypothesis);
                retval.add(job);
            }
        }
        else {
            LOG.debug("Locus {} under {} has no unknowns.", locusName, hypothesis.getId());
            LocusProbabilityJob job = new LocusProbabilityJob(locusName, crimesceneProfiles, hypothesis);
            retval.add(job);
        }
        return retval;
    }
}
