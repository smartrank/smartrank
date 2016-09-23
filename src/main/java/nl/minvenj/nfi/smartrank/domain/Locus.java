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
import java.util.Objects;
import java.util.regex.Pattern;

public class Locus {

    private static final Pattern LOCUS_NORMALIZATION_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final ArrayList<String> REGISTERED_LOCI = new ArrayList<>();
    private final String _name;
    private final ArrayList<Allele> _alleles;
    private final int _id;
    private boolean _homozygote = false;
    private Sample _sample;
    private int _hashCode;

    public static synchronized int getId(final String name) {
        final String normalized = normalize(name);
        final int index = REGISTERED_LOCI.indexOf(normalized);
        if (index >= 0) {
            return index;
        }

        REGISTERED_LOCI.add(normalized);
        return REGISTERED_LOCI.indexOf(normalized);
    }

    public static String normalize(final String name) {
        return LOCUS_NORMALIZATION_PATTERN.matcher(name).replaceAll("").toUpperCase();
    }

    public static int getRegisteredLocusCount() {
        return REGISTERED_LOCI.size();
    }

    public Locus(final String name) {
        _alleles = new ArrayList<>();
        _name = normalize(name);
        _id = getId(name);
        updateHashCode();
    }

    public String getName() {
        return _name;
    }

    public int getId() {
        return _id;
    }

    public void setSample(final Sample sample) {
        this._sample = sample;
    }

    public Sample getSample() {
        return _sample;
    }

    public String getSourceFile() {
        if (_sample != null) {
            return _sample.getSourceFile();
        }
        return "";
    }

    public String getSampleId() {
        if (_sample != null) {
            return _sample.getName();
        }
        return "";
    }

    public void setHomozygote(final boolean homozygote) {
        _homozygote = homozygote;
    }

    public boolean isHomozygote() {
        return _homozygote;
    }

    public void addAllele(final Allele allele) {
        // Let the allele know which locus it belongs to
        allele.setLocus(this);

        // If there is already an equal allele in this sample, we have a homozygote on our hands!
        setHomozygote(_alleles.contains(allele));
        _alleles.add(allele);
        updateHashCode();
    }

    public int size() {
        return _alleles.size();
    }

    public Collection<Allele> getAlleles() {
        return _alleles;
    }

    public boolean hasAllele(final String allele) {
        for (final Allele myAllele : _alleles) {
            if (myAllele.getAllele().equals(allele)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof Locus) && ((Locus) other)._hashCode == _hashCode;
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    private void updateHashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this._name);
        hash = 83 * hash + Objects.hashCode(this._alleles);
        _hashCode = hash;
    }
}
