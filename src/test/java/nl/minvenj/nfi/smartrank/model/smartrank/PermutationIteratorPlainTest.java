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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;

@RunWith(MockitoJUnitRunner.class)
public class PermutationIteratorPlainTest {

    private Locus[] _possibleAlleleCombinations;

    @Mock
    private Allele _allele1;

    @Mock
    private Allele _allele2;

    @Mock
    private Allele _allele3;

    @Mock
    private Locus _locus11;
    @Mock
    private Locus _locus12;
    @Mock
    private Locus _locus13;
    @Mock
    private Locus _locus22;
    @Mock
    private Locus _locus23;
    @Mock
    private Locus _locus33;

    @Before
    public void setup() {
        when(_allele1.getAllele()).thenReturn("1");
        when(_allele2.getAllele()).thenReturn("2");
        when(_allele3.getAllele()).thenReturn("3");

        when(_locus11.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele1));
        when(_locus12.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele2));
        when(_locus13.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele3));
        when(_locus22.getAlleles()).thenReturn(Arrays.asList(_allele2, _allele2));
        when(_locus23.getAlleles()).thenReturn(Arrays.asList(_allele2, _allele3));
        when(_locus33.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele3));

        _possibleAlleleCombinations = new Locus[]{_locus11, _locus12, _locus13, _locus22, _locus23, _locus33};
    }

    @Test
    public void testSize1Unknown() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(1, _possibleAlleleCombinations, 0);
        long result = instance.size();
        assertEquals(1, result);
    }

    @Test
    public void testSize2Unknowns() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(2, _possibleAlleleCombinations, 0);
        long result = instance.size();
        assertEquals(6, result);
    }

    @Test
    public void testSize3Unknowns() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(3, _possibleAlleleCombinations, 0);
        long result = instance.size();
        assertEquals(21, result);
    }

    @Test
    public void testHasNext() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(1, _possibleAlleleCombinations, 0);
        long count = instance.size();
        while (count > 0) {
            assertTrue("hasNext expected to be true while count<size", instance.hasNext());
            instance.next();
            count--;
        }
        assertFalse("hasNext expected to be false when count>=size", instance.hasNext());
    }

    @Test
    public void testNext() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(1, _possibleAlleleCombinations, 0);
        while (instance.hasNext()) {
            Permutation p = instance.next();
            assertNotNull(p);
            assertNotNull(p.getLoci());
        }
    }

    @Test
    public void testNextAfterEnd() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(1, _possibleAlleleCombinations, 0);
        while (instance.hasNext()) {
            instance.next();
        }
        Permutation p = instance.next();
        assertNull(p);
    }

    @Test
    public void testHasNext0Unknowns() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(0, _possibleAlleleCombinations, 0);
        while (instance.hasNext()) {
            fail("hasNext expected to return false when 0 unknowns");
        }
    }

    @Test
    public void testNext0Unknowns() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(0, _possibleAlleleCombinations, 0);
        Permutation p = instance.next();
        assertNull(p);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        PermutationIteratorPlain instance = new PermutationIteratorPlain(1, _possibleAlleleCombinations, 0);
        instance.remove();
    }

}
