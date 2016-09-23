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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;

@RunWith(MockitoJUnitRunner.class)
public class GenotypeProbabilityCalculatorFactoryTest {

    @Mock
    private Hypothesis _hypothesisThetaZero;

    @Mock
    private Hypothesis _hypothesisThetaNonZero;

    @Before
    public void setup() {
        when(_hypothesisThetaZero.getThetaCorrection()).thenReturn(0.0);
        when(_hypothesisThetaNonZero.getThetaCorrection()).thenReturn(0.03);
    }

    @Test
    public void testGetUnrelatedGenotypeProbabilityCalculator() {
        GenotypeProbabilityCalculator calculatorThetaNonZero = GenotypeProbabilityCalculatorFactory.getUnrelatedGenotypeProbabilityCalculator(_hypothesisThetaNonZero);
        assertTrue(calculatorThetaNonZero instanceof SplitDropGenotypeProbabilityCalculator);

        GenotypeProbabilityCalculator calculatorThetaZero = GenotypeProbabilityCalculatorFactory.getUnrelatedGenotypeProbabilityCalculator(_hypothesisThetaZero);
        assertTrue(calculatorThetaZero instanceof HardyWeinbergGenotypeProbabilityCalculator);
    }

    @Test
    public void testTestPrivateConstructor() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<GenotypeProbabilityCalculatorFactory> cnt = GenotypeProbabilityCalculatorFactory.class.getDeclaredConstructor();
        assertFalse(cnt.isAccessible());
        cnt.setAccessible(true);
        cnt.newInstance();
    }
}
