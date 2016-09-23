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
import java.util.Objects;

/**
 * Represents a single allele
 */
public class Allele {

    private final String _allele;
    private final float _peak;
    private Locus _locus;
    private final int _hashCode;
    private final int _id;
    private static final ArrayList<String> REGISTERED_ALLELES = new ArrayList<>();

    public static synchronized int getId(final String allele) {
        final String normalizedAllele = normalize(allele);
        final int index = REGISTERED_ALLELES.indexOf(normalizedAllele);
        if (index >= 0) {
            return index;
        }

        REGISTERED_ALLELES.add(normalizedAllele);
        return REGISTERED_ALLELES.indexOf(normalizedAllele);
    }

    public static int getRegisteredAlleleCount() {
        return REGISTERED_ALLELES.size();
    }

    public static String normalize(final String allele) {
        return allele.replaceAll("\\.0$", "");
    }

    /**
     * Constructs a new allele with the specified identifier
     *
     * @param allele The value of this allele
     */
    public Allele(final String allele) {
        this(allele, 0);
    }

    /**
     * Constructs a new allele with the specified identifier and peak
     *
     * @param allele The identifier of this allele
     * @param peak The peak value of this allele
     */
    public Allele(final String allele, final float peak) {
        _allele = normalize(allele);
        _hashCode = 23 * 7 + Objects.hashCode(_allele);
        _id = getId(allele);
        _peak = peak;
    }

    /**
     * Sets the parent locus of this allele
     *
     * @param locus The parent locus
     */
    public void setLocus(final Locus locus) {
        this._locus = locus;
    }

    /**
     * Gets the parent locus for this allele
     *
     * @return a {@link Locus} class representing the
     */
    public Locus getLocus() {
        return _locus;
    }

    /**
     * @return <b>true</b> if this allele is part of a homozygote locus
     */
    public boolean isHomozygote() {
        if (_locus != null) {
            return _locus.isHomozygote();
        }
        return false;
    }

    /**
     * @return the allele identifier for this allele
     */
    public String getAllele() {
        return _allele;
    }

    /**
     * @return An integer identifying this allele
     */
    public int getId() {
        return _id;
    }

    /**
     * @return the peak value of this allele
     */
    public float getPeak() {
        return _peak;
    }

    /**
     * @return The string representation of this allele. Essentially this is the
     * allele identifier suffixed with a single quote if the allele is part of a
     * homozygote locus
     */
    @Override
    public String toString() {
        return getAllele() + (isHomozygote() ? "'" : "");
    }

    /**
     * Compares this allele to another object and returns true if the objects
     * are equal.
     *
     * @param other The object to compare with.
     * @return true if the other object is an allele and has the same identifier
     */
    @Override
    public boolean equals(final Object other) {
        try {
            return (((Allele) other)._hashCode == _hashCode);
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }
}
