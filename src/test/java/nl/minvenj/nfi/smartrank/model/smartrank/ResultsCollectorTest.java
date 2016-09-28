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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;

@RunWith(MockitoJUnitRunner.class)
public class ResultsCollectorTest {

    @Mock
    Future<LocusProbability> _future1;

    @Mock
    Future<LocusProbability> _future2;

    @Mock
    Future<LocusProbability> _future3;

    @Mock
    Future<LocusProbability> _cancelledFuture;

    @Mock
    private LocusProbability _probability1;

    @Mock
    private LocusProbability _probability2;

    @Mock
    private LocusProbability _probability3;

    @Before
    public void setup() throws InterruptedException, ExecutionException {
        when(_probability1.getLocusName()).thenReturn("Locus1");
        when(_probability1.getValue()).thenReturn(0.01);
        when(_future1.get()).thenReturn(_probability1);

        when(_probability2.getLocusName()).thenReturn("Locus2");
        when(_probability2.getValue()).thenReturn(0.005);
        when(_future2.get()).thenReturn(_probability2);

        when(_probability3.getLocusName()).thenReturn("Locus1");
        when(_probability3.getValue()).thenReturn(0.13);
        when(_future3.get()).thenReturn(_probability3);

        when(_cancelledFuture.get()).thenThrow(new CancellationException());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunNull() {
        final ResultsCollector instance = new ResultsCollector(null);
        instance.run();
    }

    @Test
    public void testRunNonNull() throws InterruptedException, ExecutionException {
        final ResultsCollector instance = new ResultsCollector(Arrays.asList(_future1, _future2));
        instance.run();
        verify(_future1).get();
        verify(_future2).get();

        final LocusLikelihoods result = instance.getLikelihoods();
        assertNotNull(result);
        assertEquals(2, result.getLoci().size());
    }

    @Test
    public void testRun() throws InterruptedException, ExecutionException {
        final ResultsCollector instance = new ResultsCollector(Arrays.asList(_future1, _future2, _future3));
        instance.run();
        verify(_future1).get();
        verify(_future2).get();
        verify(_future3).get();

        final LocusLikelihoods result = instance.getLikelihoods();
        assertNotNull(result);
        assertEquals(2, result.getLoci().size());

        assertEquals(0.14, result.getLocusProbability("Locus1"), 0.01);
        assertEquals(0.005, result.getLocusProbability("Locus2"), 0.0001);
    }

    @Test
    public void testRunCancelled() {
        final ResultsCollector instance = new ResultsCollector(Arrays.asList(_cancelledFuture));
        instance.run();
    }

    @Test
    public void testInterrupt() {
        final ResultsCollector instance = new ResultsCollector(Arrays.asList(_future1, _future2));
        instance.interrupt();
        verify(_future1).cancel(true);
        verify(_future2).cancel(true);
    }

    @Test
    public void testGetLikelihoods() {
        final ResultsCollector instance = new ResultsCollector(new ArrayList<Future<LocusProbability>>());
        final LocusLikelihoods result = instance.getLikelihoods();
        assertNotNull(result);
        assertNotNull(result.getLoci());
        assertEquals(0, result.getLoci().size());
    }

}
