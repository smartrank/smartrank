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

package nl.minvenj.nfi.smartrank.io.databases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds statistics relating to a database.
 */
public class DatabaseStatistics {
    private final ArrayList<Integer> _specimenCountPerNumberOfLoci;
    private final HashMap<String, Integer> _specimenCountPerLocus;
    private int _recordCount;

    public DatabaseStatistics() {
        _specimenCountPerNumberOfLoci = new ArrayList<>();
        _specimenCountPerLocus = new HashMap<>();
    }

    public List<Integer> getSpecimenCountPerNumberOfLoci() {
        return _specimenCountPerNumberOfLoci;
    }

    public Map<String, Integer> getSpecimenCountPerLocus() {
        return _specimenCountPerLocus;
    }

    public int getRecordCount() {
        return _recordCount;
    }

    public void increaseRecordCount() {
        _recordCount++;
    }
}