/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.report.jasper.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesMap;

public class JasperField implements JRField {
    private static final Logger LOG = LoggerFactory.getLogger(JasperField.class);
    private String _name;
    private String _description;
    private Class<?> _aClass;
    private int _columnIndex;

    public JasperField(String name, String description, Class<?> aClass) {
        this(name, description, -1, aClass);
    }

    public JasperField(String name, String description, int columnIndex, Class<?> aClass) {
        this._name = name;
        this._description = description == null ? "" : description;
        this._aClass = aClass;
        this._columnIndex = columnIndex;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            LOG.error("Error during cloning", ex);
        }
        return null;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public void setDescription(String string) {
        _description = string;
    }

    @Override
    public Class<?> getValueClass() {
        return _aClass;
    }

    @Override
    public String getValueClassName() {
        return _aClass.getName();
    }

    @Override
    public boolean hasProperties() {
        return false;
    }

    public int getColumnIndex() {
        return _columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this._columnIndex = columnIndex;
    }

    @Override
    public JRPropertiesMap getPropertiesMap() {
        return null;
    }

    @Override
    public JRPropertiesHolder getParentProperties() {
        return null;
    }
}
