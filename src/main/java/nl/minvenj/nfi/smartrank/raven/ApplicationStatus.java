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
package nl.minvenj.nfi.smartrank.raven;

public enum ApplicationStatus {

    WAIT_DB(false, false, "Load a DNA database to continue"),
    VERIFYING_DB(true, false, "Verifying database"),
    WAIT_CRIMESCENE_PROFILES(false, false, "Load crime-scene profile(s) to continue"),
    READY_FOR_ANALYSIS(false, false, "Click the search button to start the search"),
    SEARCHING(true, true, "Running search"),
    READING_SAMPLES(true, false, "Reading Profiles"),
    WAIT_POPULATION_STATISTICS(false, false, "Load population statistics to continue"),
    LOADING_POPULATION_STATISTICS(true, false, "Loading population statistics"),
    ESTIMATING_DROPOUT(true, true, "Estimating Dropout"),
    SAVING_REPORT(true, true, "Saving report"),
    LOADING_SEARCH_CRITERIA(true, true, "Loading search criteria");

    private final boolean _active;
    private final String _message;
    private final boolean _interruptable;

    ApplicationStatus(final boolean active, final boolean interruptable, final String message) {
        _active = active;
        _interruptable = interruptable;
        _message = message;
    }

    public String getMessage() {
        return _message;
    }

    public boolean isActive() {
        return _active;
    }

    public boolean isInterruptable() {
        return _interruptable;
    }
}
