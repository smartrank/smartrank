/*
 * Copyright (C) 2016,2017 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.postprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A utility class supplying file-oriented functionality to a script.
 */
public class FileUtilitiesForScript {

    private final ConsoleWriter _consoleWriter;

    /**
     * Constructor that enabled actual file operations in the copyToDir method.
     */
    public FileUtilitiesForScript() {
        _consoleWriter = null;
    }

    /**
     * Constructor for testing purposes that will disable file operations in the copyToDir method. A message will be logged to the supplied ConsoleWriter instead.
     * @param writer a {@link ConsoleWriter} that will record the paths used in any call to the {@link FileUtilitiesForScript#copyToDir(String, String)} method
     */
    public FileUtilitiesForScript(final ConsoleWriter writer) {
        _consoleWriter = writer;
    }

    /**
     * Extracts a filename from an absolute path.
     *
     * @param fullPath The absolute path from which to extract the filename
     * @return the extracted filename
     */
    public String fileName(final String fullPath) {
        final File file = new File(fullPath);
        return file.getName();
    }

    /**
     * Copies a file to a directory.
     *
     * @param source the absolute path of the source file
     * @param target the absolute path of the destination directory
     * @throws IOException if the file could not be copied
     */
    public void copyToDir(final String source, final String target) throws IOException {
        if (_consoleWriter != null) {
            _consoleWriter.log("<font color=blue>FileUtilitiesForScript: simulating file copy<BR>&nbsp;&nbsp;&nbsp;&nbsp;Source file: '" + source + "'<br>&nbsp;&nbsp;&nbsp;&nbsp;Destination directory: '" + target + "'</font><br>");
            return;
        }

        final Path sourcePath = new File(source).toPath();

        File targetDir = new File(target);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        if (!targetDir.isDirectory()) {
            targetDir = targetDir.getParentFile();
        }
        final Path targetDirPath = targetDir.toPath();
        final String fileName = sourcePath.getFileName().toString();
        Path targetFilePath = targetDirPath.resolve(fileName);
        final int lastDotIdx = fileName.lastIndexOf('.');
        int fileIdx = 0;
        while (targetFilePath.toFile().exists()) {
            if (lastDotIdx >= 0) {
                final String deduplicatedFileName = fileName.substring(0, lastDotIdx) + "-" + fileIdx + fileName.substring(lastDotIdx);
                targetFilePath = targetDirPath.resolve(deduplicatedFileName);
            }
            else {
                targetFilePath = targetDirPath.resolve(fileName + "-" + fileIdx);
            }
            fileIdx++;
        }

        Files.copy(sourcePath, targetFilePath);
    }
}
