package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContributorTest {

    private static final double DROPOUT = 0.12;
    private static final double DROPOUT2 = 0.35;

    @Mock
    private Sample _sample;

    @Mock
    private Sample _sample2;

    @Before
    public void setUp() throws Exception {
        when(_sample.getName()).thenReturn("Sample");
    }

    @Test
    public final void testContributorSampleDouble() {
        final Contributor contributor = new Contributor(_sample, DROPOUT);
        final Contributor contributor2 = new Contributor(_sample, DROPOUT, false);
        assertEquals(_sample, contributor.getSample());
        assertFalse(contributor.isCandidate());
        assertEquals(DROPOUT, contributor.getDropoutProbability(), 0.00000001);
        compareContributors(contributor, contributor2);
    }

    @Test
    public final void testContributorSampleDoubleNotCandidate() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, false);
        assertEquals(_sample, contributor.getSample());
        assertFalse(contributor.isCandidate());
        assertEquals(DROPOUT, contributor.getDropoutProbability(), 0.00000001);
    }

    @Test
    public final void testContributorSampleDoubleCandidate() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, true);
        assertEquals(_sample, contributor.getSample());
        assertTrue(contributor.isCandidate());
        assertEquals(DROPOUT, contributor.getDropoutProbability(), 0.00000001);
    }

    @Test
    public final void testContributorContributor() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, true);
        final Contributor contributorCopy = new Contributor(contributor);
        compareContributors(contributor, contributorCopy);
    }

    private void compareContributors(final Contributor firstContributor, final Contributor secondContributor) {
        assertEquals(firstContributor.getSample(), secondContributor.getSample());
        assertEquals(firstContributor.getDropoutProbability(), secondContributor.getDropoutProbability(), 0.00000001);
        assertEquals(firstContributor.getDropOutProbability(true), secondContributor.getDropOutProbability(true), 0.00000001);
    }

    @Test
    public final void testDropOutProbability() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, true);
        compareDropout(contributor, DROPOUT);
        contributor.setDropoutProbability(DROPOUT2);
        compareDropout(contributor, DROPOUT2);
    }

    private void compareDropout(final Contributor contributor, final double dropout) {
        assertEquals(dropout, contributor.getDropoutProbability(), 0.00000001);
        assertEquals(dropout, contributor.getDropOutProbability(false), 0.00000001);
        assertEquals(dropout * dropout, contributor.getDropOutProbability(true), 0.00000001);
    }

    @Test
    public final void testSample() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, true);
        assertEquals(_sample, contributor.getSample());
        contributor.setSample(_sample2);
        assertEquals(_sample2, contributor.getSample());
    }

    @Test
    public final void testToString() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, true);
        assertEquals("Sample(0.12)", contributor.toString());
    }

    @Test
    public final void testCandidate() {
        final Contributor contributor = new Contributor(_sample, DROPOUT, false);
        assertFalse(contributor.isCandidate());
        contributor.setCandidate(true);
        assertTrue(contributor.isCandidate());
        contributor.setCandidate(false);
        assertFalse(contributor.isCandidate());
    }

}
