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
package nl.minvenj.nfi.smartrank.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Sample class represents a DNA sample containing a number of Loci which in
 * turn contain a number of alleles.
 */
public class Sample {

    private static final Logger LOG = LoggerFactory.getLogger(Sample.class);
    private String _id;
    private boolean _enabled;
    private HashMap<String, Locus> _loci;
    private final String _sourceFile;
    private String _sourceFileHash = null;
    private Locus _locus;

    /**
     * Creates a new DNASample object
     *
     * @param id The id of the sample
     */
    public Sample(final String id, final String sourceFile) {
        _loci = new LinkedHashMap<>();
        _id = id;
        _enabled = true;
        _sourceFile = sourceFile;
    }

    /**
     * Creates a new DNASample object
     *
     * @param id The id of the sample
     */
    public Sample(final String id) {
        _id = id;
        _enabled = true;
        _sourceFile = null;
    }

    /**
     * @return The getName of the sample
     */
    public String getName() {
        return _id;
    }

    public void addLocus(final Locus locus) {
        if (_locus == null && _loci == null) {
            _locus = locus;
            _locus.setSample(this);
        }
        else {
            if (_loci == null) {
                _loci = new LinkedHashMap<>();
            }
            if (_locus != null) {
                _loci.put(_locus.getName(), _locus);
                _locus = null;
            }
            _loci.put(locus.getName(), locus);
            locus.setSample(this);
        }
    }

    public int size() {
        if (_loci == null) {
            if (_locus == null) {
                return 0;
            }
            return 1;
        }
        return _loci.size();
    }

    public Locus getLocus(final String id) {
        if (_locus == null) {
            if (_loci == null) {
                return null;
            }
            return _loci.get(id);
        }
        if (_locus.getName().equals(id)) {
            return _locus;
        }
        return null;
    }

    public Collection<Locus> getLoci() {
        if (_loci != null) {
            return _loci.values();
        }
        final List<Locus> retval = new ArrayList<>();
        if (_locus != null) {
            retval.add(_locus);
        }
        return retval;
    }

    public boolean hasLocus(final String name) {
        return _loci != null && _loci.containsKey(name);
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isEnabled() {
        return _enabled;
    }

    public void setEnabled(final boolean enabled) {
        LOG.debug("Sample {} setEnabled({})", _id, enabled);
        _enabled = enabled;
    }

    /**
     * @return the sourceFile
     */
    public String getSourceFile() {
        return _sourceFile;
    }

    /**
     * Sets the file hash of the source file
     *
     * @param fileHash A String containing the hash of the source file
     */
    public void setSourceFileHash(final String fileHash) {
        _sourceFileHash = fileHash;
    }

    /**
     * @return A String containing the hash over the contents of the source file
     */
    public String getSourceFileHash() {
        return _sourceFileHash;
    }

    /**
     * Sets a new ID for this sample. Useful when reading Genemapper files for samples where the getNames for all replicates may be the same, and we have to add a postfix.
     *
     * @param id The new ID of this sample
     */
    public void setName(final String id) {
        _id = id;
    }

    /**
     * Removes a locus from the sample.
     *
     * @param locus The locus to remove
     */
    public void removeLocus(final Locus locus) {
        if (_loci != null)
            _loci.remove(locus.getName());
        if (_locus != null && _locus.equals(locus))
            _locus = null;
    }
}
