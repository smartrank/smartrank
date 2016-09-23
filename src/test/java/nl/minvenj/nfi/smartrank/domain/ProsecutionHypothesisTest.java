package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionHypothesisTest {

    @Mock
    private Sample _sample;

    @Test
    public void testSetCandidate() {
        final ProsecutionHypothesis hypo = new ProsecutionHypothesis();
        hypo.setCandidate(_sample);
    }

    @Test
    public void testCopy() {
        final ProsecutionHypothesis hypothesis = new ProsecutionHypothesis();
        final Hypothesis hypothesis2 = hypothesis.copy();
        assertNotNull(hypothesis2);
        assertTrue("Copy is expected to be equal to original", hypothesis.toString().equals(hypothesis2.toString()));
    }

    @Test
    public void testGetCandidate() {
        final ProsecutionHypothesis hypo = new ProsecutionHypothesis();
        hypo.setCandidate(_sample);
        assertEquals(_sample, hypo.getCandidate());
    }

    @Test
    public void testHasCandidate() {
        final ProsecutionHypothesis hypo = new ProsecutionHypothesis();
        hypo.setCandidate(_sample);
        assertTrue(hypo.hasCandidate());
    }

    @Test
    public void testIsQDesignationShutdown() {
        final ProsecutionHypothesis hypo = new ProsecutionHypothesis();
        assertTrue(hypo.isQDesignationShutdown());
    }

    @Test
    public void testProsecutionHypothesis() {
        final ProsecutionHypothesis hypo = new ProsecutionHypothesis();
        assertEquals("Prosecution", hypo.getId());
    }

    @Test
    public void testReset() {
        final ProsecutionHypothesis hypo = new ProsecutionHypothesis();
        hypo.setCandidate(_sample);
        hypo.reset();
        assertFalse(_sample.equals(hypo.getCandidate()));
    }

}
