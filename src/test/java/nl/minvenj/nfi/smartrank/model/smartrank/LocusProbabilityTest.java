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
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;

@RunWith(MockitoJUnitRunner.class)
public class LocusProbabilityTest {

    @Mock
    private Hypothesis _hypothesis;

    @Test
    public void testSetValue() {
        LocusProbability instance = new LocusProbability(_hypothesis, "TestLocus");
        instance.setValue(2.0);
        assertEquals(2.0, instance.getValue(), 0.0);
    }

    @Test
    public void testAddValue() {
        LocusProbability instance = new LocusProbability(_hypothesis, "TestLocus");
        instance.setValue(2.0);
        assertEquals(2.0, instance.getValue(), 0.0);
        instance.addValue(0.5);
        assertEquals(2.5, instance.getValue(), 0.0);
    }

    @Test
    public void testGetHypothesis() {
        LocusProbability instance = new LocusProbability(_hypothesis, "TestLocus");
        assertSame(_hypothesis, instance.getHypothesis());
    }

    @Test
    public void testGetLocusName() {
        LocusProbability instance = new LocusProbability(_hypothesis, "TestLocus");
        assertSame("TestLocus", instance.getLocusName());
    }

}