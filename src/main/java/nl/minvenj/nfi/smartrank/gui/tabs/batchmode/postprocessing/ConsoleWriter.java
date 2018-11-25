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

package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.postprocessing;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Writer that directs output to a component.
 *
 */
public class ConsoleWriter extends Writer {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWriter.class);
    private final JTextPane _console;

    /**
     * Constructor that redirects output to a supplied text component.
     *
     * @param console the component that will receive text supplied through the write and log methods
     */
    public ConsoleWriter(final JTextPane console) {
        _console = console;
    }

    /**
     * Constructor that will cause any text supplied to the log and write methods to be discarded
     */
    public ConsoleWriter() {
        _console = null;
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        log(new String(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Sends the supplied text to the component supplied in the constructor, of discards this text if the component is null.
     *
     * @param string the string to output
     */
    public void log(final String string) {
        if (_console != null) {
            try {
                final HTMLDocument doc = (HTMLDocument) _console.getStyledDocument();
                doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), string + "<br>");
            }
            catch (final Exception e) {
                LOG.warn("Cannot update javascript console!", e);
            }
        }
    }
}
