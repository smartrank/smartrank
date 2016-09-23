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
package nl.minvenj.nfi.smartrank.model.smartrank;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;

/**
 * A Future&gt;Double&lt; that returns a pre-determined Double from its
 * <code>get</code> method. Used in the SmartRankModel to pass a previously
 * calculated result in the same way as a newly calculated result.
 */
class FixedResultJob implements Future<LocusProbability> {

    private final LocusProbability _locusProbability;

    /**
     * Constructor.
     *
     * @param hypothesis  The hypothesis under which evaluation was performed
     * @param locusName   The name of the locus to which the result applies
     * @param probability The probability og the hypothesis at the named locus
     */
    public FixedResultJob(Hypothesis hypothesis, String locusName, double probability) {
        _locusProbability = new LocusProbability(hypothesis, locusName);
        _locusProbability.setValue(probability);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public LocusProbability get() throws InterruptedException, ExecutionException {
        return _locusProbability;
    }

    @Override
    public LocusProbability get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }

}
