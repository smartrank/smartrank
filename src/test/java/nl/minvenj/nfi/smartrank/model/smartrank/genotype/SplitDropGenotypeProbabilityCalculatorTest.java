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

package nl.minvenj.nfi.smartrank.model.smartrank.genotype;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;

@RunWith(MockitoJUnitRunner.class)
public class SplitDropGenotypeProbabilityCalculatorTest {

    @Mock
    private Hypothesis _hypothesis;
    
    @Mock
    private PopulationStatistics _populationStatistics;
    
    @Mock
    private Locus _heterozygoteLocus;
    
    @Mock
    private Locus _homozygoteLocus;
    
    @Mock
    private Allele _allele1;
    
    @Mock
    private Allele _allele2;
    
    @Before
    public void setup() {
        when(_allele1.getAllele()).thenReturn("Allele1");
        when(_allele2.getAllele()).thenReturn("Allele2");
        when(_allele1.getId()).thenReturn(0);
        when(_allele2.getId()).thenReturn(1);

        when(_populationStatistics.getProbability(any(Locus.class), eq(_allele1))).thenReturn(0.15);
        when(_populationStatistics.getProbability(any(Locus.class), eq(_allele2))).thenReturn(0.35);

        when(_heterozygoteLocus.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele2));
        when(_heterozygoteLocus.getName()).thenReturn("SomeName");

        when(_homozygoteLocus.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele1));
        when(_homozygoteLocus.getName()).thenReturn("SomeName");
        when(_homozygoteLocus.isHomozygote()).thenReturn(Boolean.TRUE);

        when(_hypothesis.getPopulationStatistics()).thenReturn(_populationStatistics);
        when(_hypothesis.getThetaCorrection()).thenReturn(0.003);
    }
    
    @Test
    public void testCalculateHomozygote() {
        int[] alleleCounts = new int[10];
        SplitDropGenotypeProbabilityCalculator instance = new SplitDropGenotypeProbabilityCalculator(_hypothesis);
        double result = instance.calculate(alleleCounts, _homozygoteLocus);
        assertEquals(0.0228138524, result, 0.000000001);
        assertEquals(2, alleleCounts[_allele1.getId()]);
    }

    @Test
    public void testCalculateHeterozygote() {
        int[] alleleCounts = new int[10];
        SplitDropGenotypeProbabilityCalculator instance = new SplitDropGenotypeProbabilityCalculator(_hypothesis);
        double result = instance.calculate(alleleCounts, _heterozygoteLocus);
        assertEquals(0.104370, result, 0.000001);
        assertEquals(1, alleleCounts[_allele1.getId()]);
        assertEquals(1, alleleCounts[_allele2.getId()]);
    }
}
