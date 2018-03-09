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
package nl.minvenj.nfi.smartrank.io.databases;

public interface DatabaseValidationEventListener {

    /**
     * Called when the next record is parsed. The method is called regardless of
     * whether parsing succeeds.
     *
     * @param current Current value. Depending on the reader this can be the
     *                current record number, file offset or any other numerical
     *                progress value.
     * @param max     Maximum value. Depending on the reader this can be total
     *                record count, file size or any other numerical progress
     *                value.
     */
    public void onProgress(long current, long max);

    /**
     * Called when the reader encounters a problem in the database.
     *
     * @param specimen
     * @param locus
     * @param message  A description of the encountered problem
     */
    public void onProblem(String specimen, String locus, String message);
}
