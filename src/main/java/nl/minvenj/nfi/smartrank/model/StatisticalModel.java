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

package nl.minvenj.nfi.smartrank.model;

import java.security.Policy.Parameters;

import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;

public interface StatisticalModel {

    /**
     * Calculates the likelihood of the evidence described in the {@link Parameters} given the supplied {@link Hypothesis}.
     *
     * @param hypothesis the {@link Hypothesis} for which to calculate the likelihood.
     * @param parameters the {@link Parameters} describing the evidence and search parameters.
     *
     * @return a {@link LocusLikelihoods} object containing the likelihood of the evidence under the given hypothesis.
     */
    public LocusLikelihoods calculateLikelihood(Hypothesis hd, AnalysisParameters parameters) throws InterruptedException;

    /**
     * Interrupts the calculation of the likelihood.
     */
    public void interrupt();

    /**
     * Gets the model name.
     *
     * @return a String containing the model name.
     */
    public String getModelName();

    /**
     * Resets the internal state of the model.
     */
    public void reset();
}
