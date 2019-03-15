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
package nl.minvenj.nfi.smartrank.gui.tabs.batchmode.actionlisteners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;

/**
 * Stop the search process when the user clicks the stop button.
 */
public final class StopActionListener implements ActionListener {

    private final BatchModePanel _batchModePanel;

    /**
     * Constructor.
     *
     * @param batchModePanel the parent panel
     */
    public StopActionListener(final BatchModePanel batchModePanel) {
        _batchModePanel = batchModePanel;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        _batchModePanel._stopButton.setEnabled(false);
        _batchModePanel._running.set(false);
    }
}