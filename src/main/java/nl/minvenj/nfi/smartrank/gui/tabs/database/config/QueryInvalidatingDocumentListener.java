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
package nl.minvenj.nfi.smartrank.gui.tabs.database.config;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;

/**
 * Marks the configured queries as unvalidated, triggering a validation on the next attempt to connect to the database.
 */
final class QueryInvalidatingDocumentListener implements DocumentListener {
    @Override
    public void removeUpdate(final DocumentEvent e) {
        onChanged();
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        onChanged();
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        onChanged();
    }

    private void onChanged() {
        SmartRankGUISettings.setDatabaseQueriesValidated(false);
    }
}