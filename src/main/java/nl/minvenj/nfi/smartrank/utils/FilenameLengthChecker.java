/*
 * Copyright (C) 2017 Netherlands Forensic Institute
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

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameLengthChecker {

    private static final Logger LOG = LoggerFactory.getLogger(FilenameLengthChecker.class);

    private FilenameLengthChecker() {
        // Private constructor to make CheckStyle shut up
    }

    /**
     * Checks a file path for a predefined maximum length and shortens the filename to stay within defined limits.
     *
     * @param absolutePath The path to check
     * @param deduplicatingFilter a {@link FileFilter} that is applies to shortened filenames to determine if the filename is unique
     * @param maxPathLength the maximum path length
     * @return a string holding a potentially shortened version of the input path
     */
    public static String check(final String absolutePath, final FileFilter deduplicatingFilter, final int maxPathLength) {
        if (absolutePath.length() >= maxPathLength) {
            String baseName = FilenameUtils.getBaseName(absolutePath);
            final String extension = FilenameUtils.getExtension(absolutePath);
            final String path = FilenameUtils.getFullPath(absolutePath);
            final int maxBasenameLength = maxPathLength - path.length() - 1 - extension.length() - 1;

            // If the selected path is longer than the maximum, we cannot perform our magic on the filename, so
            // just return the input value and leave a line in the log to the effect that this file may not be
            // readable on all operating systems.
            if (maxBasenameLength < 4) {
                LOG.warn("Path length exceeds {} characters. This file may not be readable on some operating systems: {}", maxPathLength, absolutePath);
                return absolutePath;
            }

            if (baseName.length() > maxBasenameLength) {
                baseName = baseName.substring(0, maxBasenameLength);
            }

            File file = new File(path, baseName + "." + extension);
            int idx = 0;
            String name = baseName;
            while (!deduplicatingFilter.accept(file)) {
                final String postFix = "(" + idx + ")";
                name = baseName + postFix + "." + extension;
                final int postFixIdx = baseName.length();
                name = name.substring(0, postFixIdx - postFix.length()) + name.substring(postFixIdx);
                file = new File(path, name);
                idx++;
            }

            final int unixIdx = absolutePath.indexOf("/");
            final int windowsIdx = absolutePath.indexOf("\\");
            if (windowsIdx < 0 || (unixIdx < windowsIdx && unixIdx >= 0)) {
                return FilenameUtils.separatorsToUnix(file.getPath());
            }
            return FilenameUtils.separatorsToWindows(file.getPath());
        }
        return absolutePath;
    }
}
