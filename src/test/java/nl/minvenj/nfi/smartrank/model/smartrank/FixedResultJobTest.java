package nl.minvenj.nfi.smartrank.model.smartrank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;

@RunWith(MockitoJUnitRunner.class)
public class FixedResultJobTest {

    @Mock
    Hypothesis _hypothesis;

    @Test
    public void testCancel() {

        FixedResultJob instance = new FixedResultJob(null, null, 0.0);
        assertTrue(instance.cancel(true));
        assertTrue(instance.cancel(false));
    }

    @Test
    public void testIsCancelled() {
        FixedResultJob instance = new FixedResultJob(null, null, 0.0);
        assertFalse(instance.isCancelled());
    }

    @Test
    public void testIsDone() {
        FixedResultJob instance = new FixedResultJob(null, null, 0.0);
        assertTrue(instance.isDone());
    }

    @Test
    public void testGet_0args() throws Exception {
        FixedResultJob instance = new FixedResultJob(_hypothesis, "TestLocus", 1.2);
        assertFalse(instance.isCancelled());
        LocusProbability result = instance.get();
        assertEquals(1.2, result.getValue(), 0.0);
        assertEquals(_hypothesis, result.getHypothesis());
        assertEquals("TestLocus", result.getLocusName());
    }

    @Test
    public void testGet_long_TimeUnit() throws Exception {
        FixedResultJob instance = new FixedResultJob(_hypothesis, "TestLocus", 1.2);
        assertFalse(instance.isCancelled());
        long timeout = 0L;
        TimeUnit unit = null;
        LocusProbability result = instance.get(timeout, unit);
        assertEquals(1.2, result.getValue(), 0.0);
        assertEquals(_hypothesis, result.getHypothesis());
        assertEquals("TestLocus", result.getLocusName());
    }

}
