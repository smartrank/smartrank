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

package nl.minvenj.nfi.smartrank.analysis.parameterestimation;

/**
 * Defines the interface for classes that listen for the progress of a dropout estimation.
 */
public interface DropoutProgressListener {

    /**
     * Called to set the number of iterations.
     *
     * @param iterations the number of iterations in the dropout estimation
     */
    public void setIterations(int iterations);

    /**
     * Called to signal that an iteration is done.
     */
    public void onIterationDone();
}
