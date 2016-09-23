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

package nl.minvenj.nfi.smartrank.model.smartrank;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.Locus;

public class PermutationTest {

    @Test
    public void testConstructorNull() {
        new Permutation(null, 0);
    }

    @Test
    public void testConstructorEmptyArray() {
        new Permutation(new Locus[0], 0);
    }

    @Test
    public void testGetLoci() {
        final Locus[] locusArray = new Locus[]{new Locus("Locus1")};
        final Permutation instance = new Permutation(locusArray, 1);
        final Locus[] result = instance.getLoci();
        assertArrayEquals(locusArray, result);
    }

    @Test
    public void testGetPermutationFactor() {
        final Locus[] locusArray = new Locus[]{new Locus("Locus1")};
        final Permutation instance = new Permutation(locusArray, 123);
        final int result = instance.getPermutationFactor();
        assertEquals(123, result);
    }

}