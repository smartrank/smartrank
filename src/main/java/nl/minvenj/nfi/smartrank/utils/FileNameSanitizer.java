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
package nl.minvenj.nfi.smartrank.utils;

/**
 * Sanitizes a filename by replacing invalid charaters with '-'.
 */
public class FileNameSanitizer {
    private FileNameSanitizer() {
    }

    /**
     * Sanitizes the supplied input so that it can be used as a filename. Valid characters are:
     * A to Z, a to z, 0 to 9 and .-_#()[]
     *
     * @param input the string to sanitize
     * @return the input string with invalid characters replaced by '-'.
     */
    public static String sanitize(final String input) {
        if (input == null)
            return null;
        return input.replaceAll("[^a-zA-Z0-9\\.\\-_#\\@\\(\\)\\[\\]]", "-");
    }
}
