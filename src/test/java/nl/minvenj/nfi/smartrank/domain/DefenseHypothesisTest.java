package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefenseHypothesisTest {

    @Mock
    private Sample _sample;

    @Test
    public void testSetCandidate() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        hypothesis.setCandidate(_sample);
    }

    @Test
    public void testCopy() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        final Hypothesis hypothesis2 = hypothesis.copy();
        assertNotNull(hypothesis2);
        assertTrue("Copy is expected to be equal to original", hypothesis.toString().equals(hypothesis2.toString()));
    }

    @Test
    public void testGetCandidate() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        hypothesis.setCandidate(_sample);
        assertNull(hypothesis.getCandidate());
    }

    @Test
    public void testHasCandidate() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        hypothesis.setCandidate(_sample);
        assertFalse(hypothesis.hasCandidate());
    }

    @Test
    public void testIsQDesignationShutdown() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        assertFalse(hypothesis.isQDesignationShutdown());
    }

    @Test
    public void testDefenseHypothesis() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        assertTrue(hypothesis instanceof Hypothesis);
        assertEquals(hypothesis.getId(), "Defense");
    }

    @Test
    public void testReset() {
        final DefenseHypothesis hypothesis = new DefenseHypothesis();
        hypothesis.reset();
    }

}
