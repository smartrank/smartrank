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
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

@RunWith(MockitoJUnitRunner.class)
public class LocusProbabilityJobGeneratorTest {

    private final String _locusName = "SomeLocus";

    @Mock
    private AnalysisParameters _parameters;

    @Mock
    private Hypothesis _hypothesis;

    @Mock
    private Sample _sample;

    @Mock
    private Locus _locus;

    @Mock
    private Allele _allele;

    @Before
    public void setup() {
        when(_parameters.getEnabledCrimesceneProfiles()).thenReturn(Arrays.asList(_sample));
        when(_locus.getAlleles()).thenReturn(Arrays.asList(_allele, _allele));
    }

    @Test
    public void testTestPrivateConstructor() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<LocusProbabilityJobGenerator> cnt = LocusProbabilityJobGenerator.class.getDeclaredConstructor();
        assertFalse(cnt.isAccessible());
        cnt.setAccessible(true);
        cnt.newInstance();
    }

    @Test
    public void testGenerateNoUnknowns() {
        Locus[] possibleAlleleCombinations = null;
        ArrayList<LocusProbabilityJob> result = LocusProbabilityJobGenerator.generate(_locusName, _parameters, possibleAlleleCombinations, _hypothesis);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGenerate1Unknown() {
        Locus[] possibleAlleleCombinations = new Locus[]{_locus};
        when(_hypothesis.getUnknownCount()).thenReturn(1);
        ArrayList<LocusProbabilityJob> result = LocusProbabilityJobGenerator.generate(_locusName, _parameters, possibleAlleleCombinations, _hypothesis);
        assertNotNull(result);
    }
}
