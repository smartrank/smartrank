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

package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocusTest {

    @Mock
    private Sample _sample;

    @Mock
    private Allele _allele1;

    @Mock
    private Allele _allele2;

    @Mock
    private Allele _otherAllele;

    @Test
    public void testGetId_String() {
        final String someName = "SomeName";
        final String someOtherName = "SomeOtherName";
        final int someId = Locus.getId(someName);
        final int someOtherId = Locus.getId(someOtherName);
        assertFalse(someId == someOtherId);
        final int sameSomeId = Locus.getId(someName);
        assertEquals(someId, sameSomeId);
    }

    @Test
    public void testGetRegisteredLocusCount() {
        final int baseLine = Locus.getRegisteredLocusCount();
        Locus.getId("testGetRegisteredLocusCount1");
        assertTrue((baseLine + 1) == Locus.getRegisteredLocusCount());
        Locus.getId("testGetRegisteredLocusCount2");
        assertTrue((baseLine + 2) == Locus.getRegisteredLocusCount());
        Locus.getId("Test Get Registered LOCUS Count 1");
        assertTrue((baseLine + 2) == Locus.getRegisteredLocusCount());
    }

    @Test
    public void testGetName() {
        final String name = "LocusName";
        final Locus instance = new Locus(name);
        final String result = instance.getName();
        assertEquals(name.toUpperCase(), result);
    }

    @Test
    public void testGetId_0args() {
        final Locus instance = new Locus("SomeName");
        final int expResult = Locus.getId("SomeName");
        final int result = instance.getId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetSample() {
        final Locus instance = new Locus("SomeName");
        instance.setSample(_sample);
    }

    @Test
    public void testGetSample() {
        final Locus instance = new Locus("SomeName");
        instance.setSample(_sample);
        final Sample result = instance.getSample();
        assertEquals(_sample, result);
    }

    @Test
    public void testGetSourceFile() {
        final Locus instance = new Locus("SomeName");
        final String expResult = "SomeSourceFile";
        assertEquals("", instance.getSourceFile());

        when(_sample.getSourceFile()).thenReturn(expResult);
        instance.setSample(_sample);
        final String result = instance.getSourceFile();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSampleId() {
        final String expResult = "SomeSampleName";
        final Locus instance = new Locus("SomeName");
        assertEquals("", instance.getSampleId());

        when(_sample.getName()).thenReturn(expResult);
        instance.setSample(_sample);
        final String result = instance.getSampleId();
        assertEquals(expResult, result);
    }

    @Test
    public void testHomozygote() {
        final Locus instance = new Locus("SomeName");
        assertFalse(instance.isHomozygote());
        instance.setHomozygote(true);
        assertTrue(instance.isHomozygote());
        instance.setHomozygote(false);
        assertFalse(instance.isHomozygote());
    }

    @Test
    public void testAddAllele() {
        final Locus instance = new Locus("SomeName");
        instance.addAllele(_allele2);
        verify(_allele2).setLocus(instance);
    }

    @Test
    public void testSize() {
        final Locus instance = new Locus("SomeName");
        instance.addAllele(_allele2);
        assertEquals(1, instance.size());
        instance.addAllele(_allele2);
        assertEquals(2, instance.size());
    }

    @Test
    public void testGetAllelesEmptyLocus() {
        final Locus instance = new Locus("SomeName");
        final Collection<Allele> result = instance.getAlleles();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllelesNonEmptyLocus() {
        final Locus instance = new Locus("SomeName");
        instance.addAllele(_allele2);
        final Collection<Allele> result = instance.getAlleles();
        assertNotNull(result);
        assertEquals(1, result.size());

        instance.addAllele(_otherAllele);
        assertEquals(2, result.size());
    }

    @Test
    public void testHasAllele() {
        final Locus instance = new Locus("SomeName");
        final String alleleName = "1";
        when(_allele1.getAllele()).thenReturn(alleleName);
        when(_allele2.getAllele()).thenReturn("2");
        instance.addAllele(_allele2);
        assertFalse(instance.hasAllele(alleleName));
        instance.addAllele(_allele1);
        assertTrue(instance.hasAllele(alleleName));
    }

    @Test
    public void testToString() {
        final Locus instance = new Locus("SomeName");
        assertEquals("SOMENAME", instance.toString());
        instance.addAllele(_allele2);
        assertEquals("SOMENAME", instance.toString());
        instance.addAllele(_otherAllele);
        assertEquals("SOMENAME", instance.toString());
    }

    @Test
    public void testEquals() {
        final Locus instance = new Locus("SomeLocus");
        instance.addAllele(_allele2);

        final Locus identicalInstance = new Locus("SomeLocus");
        identicalInstance.addAllele(_allele2);

        final Locus identicalInstanceWithDifferentName = new Locus("SomeLocusWithADifferentName");
        identicalInstanceWithDifferentName.addAllele(_allele2);

        final Locus differentInstance = new Locus("DifferentLocus");
        differentInstance.addAllele(_allele2);
        differentInstance.addAllele(_otherAllele);

        assertTrue(instance.equals(instance));
        assertTrue(instance.equals(identicalInstance));
        assertFalse(instance.equals("This is not a Locus but a String"));
        assertFalse(instance.equals(differentInstance));
        assertFalse(instance.equals(identicalInstanceWithDifferentName));
    }

    @Test
    public void testHashCode() {
        final Locus instance1 = new Locus("SomeLocus");
        final Locus instance2 = new Locus("SomeOtherLocus");

        final int hash11 = instance1.hashCode();
        final int hash21 = instance2.hashCode();

        assertTrue(hash11 != hash21);

        instance1.addAllele(_allele2);
        final int hash12 = instance1.hashCode();

        assertTrue(hash12 != hash11);
    }

}