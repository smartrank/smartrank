package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HypothesisTest {

    private static final String TEST_HYPOTHESIS_ID = "TestHypothesis";
    private static final String TEST_SAMPLE1_NAME = "TestSample1";
    private static final String TEST_SAMPLE1_SOURCEFILE = "SourceFile1";
    private static final String TEST_SAMPLE2_NAME = "TestSample2";
    private static final String TEST_SAMPLE2_SOURCEFILE = "SourceFile2";

    @Mock
    private Sample _sample1;

    @Mock
    private Sample _sample1a;

    @Mock
    private Sample _sample1b;

    @Mock
    private Sample _sample2;

    @Mock
    private Sample _sample2a;

    @Mock
    private PopulationStatistics _stats;

    @Mock
    private Allele _allele2;

    @Mock
    private Locus _locus2;

    @Before
    public void setupMocks() {
        when(_sample1.getName()).thenReturn(TEST_SAMPLE1_NAME);
        when(_sample1.getSourceFile()).thenReturn(TEST_SAMPLE1_SOURCEFILE);

        when(_sample1a.getName()).thenReturn(TEST_SAMPLE1_NAME);
        when(_sample1a.getSourceFile()).thenReturn(TEST_SAMPLE2_SOURCEFILE);

        when(_sample1b.getName()).thenReturn(TEST_SAMPLE2_NAME);
        when(_sample1b.getSourceFile()).thenReturn(TEST_SAMPLE1_SOURCEFILE);

        when(_sample2.getName()).thenReturn(TEST_SAMPLE2_NAME);
        when(_sample2.getSourceFile()).thenReturn(TEST_SAMPLE2_SOURCEFILE);
        when(_sample2.getLoci()).thenReturn(Arrays.asList(_locus2));
        when(_locus2.getSample()).thenReturn(_sample2);
        when(_allele2.getAllele()).thenReturn("2");
        when(_allele2.getLocus()).thenReturn(_locus2);

        when(_sample2a.getName()).thenReturn(TEST_SAMPLE1_NAME);
        when(_sample2a.getSourceFile()).thenReturn(TEST_SAMPLE2_SOURCEFILE);
    }

    @Test
    public void testNewHypothesis() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertTrue(hypo.getContributors().isEmpty());
        assertEquals(0, hypo.getUnknownCount());
    }

    @Test
    public void testAddNewContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        final Contributor c = hypo.addContributor(_sample1, 0.02);
        assertNotNull(c);
        assertEquals(_sample1, c.getSample());
        assertEquals(0.02, c.getDropoutProbability(), 0.00000001);
    }

    @Test
    public void testAddDuplicateContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        final Contributor c1 = hypo.addContributor(_sample1, 0.02);
        assertNotNull(c1);
        assertEquals(_sample1, c1.getSample());
        assertEquals(0.02, c1.getDropoutProbability(), 0.00000001);
        final Contributor c2 = hypo.addContributor(_sample1, 0.04);
        assertNotNull(c2);
        assertEquals(c1, c2);
        assertEquals(_sample1, c2.getSample());
        assertEquals(0.04, c2.getDropoutProbability(), 0.00000001);
    }

    @Test
    public void testAddExistingNonContributorAsContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addNonContributor(_sample1, 0.02);
        final Contributor c2 = hypo.addContributor(_sample1, 0.04);
        assertNotNull(c2);
        assertEquals(_sample1, c2.getSample());
        assertEquals(0.04, c2.getDropoutProbability(), 0.00000001);
    }

    @Test
    public void testAddNewNonContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertFalse(hypo.isContributor(_sample1));
        hypo.addNonContributor(_sample1, 0);
        final Contributor c = hypo.getContributor(TEST_SAMPLE1_NAME);
        assertNotNull(c);
    }

    @Test
    public void testAddDuplicateNonContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertFalse(hypo.isContributor(_sample1));
        hypo.addNonContributor(_sample1, 0.01);
        final Contributor c1 = hypo.getContributor(TEST_SAMPLE1_NAME);
        assertNotNull(c1);
        assertEquals(0.01, c1.getDropoutProbability(), 0.00000001);
        hypo.addNonContributor(_sample1, 0.04);
        final Contributor c2 = hypo.getContributor(TEST_SAMPLE1_NAME);
        assertNotNull(c2);
        assertEquals(c1, c2);
        assertEquals(0.04, c2.getDropoutProbability(), 0.00000001);
    }

    @Test
    public void testAddExistingContributorAsNonContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0.02);
        hypo.addNonContributor(_sample1, 0);
        final Collection<Contributor> contributors = hypo.getContributors();
        assertNotNull(contributors);
        assertTrue(contributors.isEmpty());
        final Collection<Contributor> noncontributors = hypo.getNonContributors();
        assertNotNull(noncontributors);
        assertEquals(1, noncontributors.size());
    }

    @Test
    public void testUnknownCount() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertEquals(0, hypo.getUnknownCount());
        hypo.setUnknownCount(2);
        assertEquals(2, hypo.getUnknownCount());
    }

    @Test
    public void testGetId() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertEquals(TEST_HYPOTHESIS_ID, hypo.getId());
    }

    @Test
    public void testGetContributors() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        Collection<Contributor> contributors = hypo.getContributors();
        assertNotNull(contributors);
        assertTrue(contributors.isEmpty());
        hypo.addContributor(_sample1, 0.02);
        contributors = hypo.getContributors();
        assertNotNull(contributors);
        assertEquals(1, contributors.size());
        final Sample sample = contributors.iterator().next().getSample();
        assertEquals(_sample1, sample);
    }

    @Test
    public void testGetNonContributors() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        Collection<Contributor> noncontributors = hypo.getNonContributors();
        assertNotNull(noncontributors);
        assertTrue(noncontributors.isEmpty());
        hypo.addNonContributor(_sample1, 0.02);
        noncontributors = hypo.getNonContributors();
        assertNotNull(noncontributors);
        assertEquals(1, noncontributors.size());
        final Sample sample = noncontributors.iterator().next().getSample();
        assertEquals(_sample1, sample);
    }

    @Test
    public void testRemoveNonExistingContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0);
        hypo.removeContributor(_sample2);
        assertTrue(hypo.isContributor(_sample1));
    }

    @Test
    public void testRemoveNonExistingNonContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addNonContributor(_sample1, 0);
        hypo.removeContributor(_sample2);
        final Collection<Contributor> nonContributors = hypo.getNonContributors();
        assertNotNull(nonContributors);
        assertEquals(1, nonContributors.size());
        final Sample sample = hypo.getNonContributors().iterator().next().getSample();
        assertEquals(_sample1, sample);
    }

    @Test
    public void testRemoveExistingContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0);
        hypo.removeContributor(_sample1);
        final Collection<Sample> samples = hypo.getSamples();
        assertNotNull(samples);
        assertTrue(samples.isEmpty());
        final Collection<Contributor> contributors = hypo.getContributors();
        assertNotNull(contributors);
        assertTrue(contributors.isEmpty());
    }

    @Test
    public void testRemoveExistingNonContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addNonContributor(_sample1, 0);
        hypo.removeContributor(_sample1);
        final Collection<Sample> samples = hypo.getSamples();
        assertNotNull(samples);
        assertTrue(samples.isEmpty());
        final Collection<Contributor> contributors = hypo.getContributors();
        assertNotNull(contributors);
        assertTrue(contributors.isEmpty());
    }

    @Test
    public void testGetContributorSamples() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        Collection<Sample> samples = hypo.getSamples();
        assertNotNull(samples);
        assertTrue(samples.isEmpty());
        hypo.addContributor(_sample1, 0);
        samples = hypo.getSamples();
        assertNotNull(samples);
        assertEquals(1, samples.size());
        final Sample sample = samples.iterator().next();
        assertEquals(_sample1, sample);
    }

    @Test
    public void testGetNonContributorSamples() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        Collection<Sample> samples = hypo.getSamples();
        assertNotNull(samples);
        assertTrue(samples.isEmpty());
        hypo.addNonContributor(_sample1, 0);
        samples = hypo.getSamples();
        assertNotNull(samples);
        assertEquals(1, samples.size());
        final Sample sample = samples.iterator().next();
        assertEquals(_sample1, sample);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetPopulationStatistics() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.getPopulationStatistics();
    }

    @Test
    public void testSetStatistics() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.setStatistics(_stats);
        assertEquals(_stats, hypo.getPopulationStatistics());
    }

    @Test
    public void testDropInProbability() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertEquals(0, hypo.getDropInProbability(), 0.001);
        hypo.setDropInProbability(0.05);
        assertEquals(0.05, hypo.getDropInProbability(), 0.001);
    }

    @Test
    public void testThetaCorrection() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertEquals(0, hypo.getThetaCorrection(), 0.001);
        hypo.setThetaCorrection(0.05);
        assertEquals(0.05, hypo.getThetaCorrection(), 0.001);
    }

    @Test
    public void testUnknownDropoutProbability() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertEquals(0, hypo.getUnknownDropoutProbability(), 0.001);
        hypo.setUnknownDropoutProbability(0.05);
        assertEquals(0.05, hypo.getUnknownDropoutProbability(), 0.001);
    }

    @Test
    public void testGetContributorSampleContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0);
        final Contributor addedContributor = hypo.addContributor(_sample2, 0);
        assertNotNull(addedContributor);
        final Contributor foundContributor = hypo.getContributor(_sample2);
        assertEquals(addedContributor, foundContributor);
    }

    @Test
    public void testGetContributorSampleNonContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addNonContributor(_sample1, 0.12);
        hypo.addNonContributor(_sample2, 0.34);
        final Contributor foundContributor = hypo.getContributor(_sample2);
        assertEquals(_sample2, foundContributor.getSample());
        assertEquals(0.34, foundContributor.getDropoutProbability(), 0.00000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetContributorSampleNonexistant() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0);
        hypo.getContributor(_sample2);
    }

    @Test
    public void testGetContributorAlleleContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0);
        final Contributor addedContributor = hypo.addContributor(_sample2, 0);
        assertNotNull(addedContributor);
        final Contributor foundContributor = hypo.getContributor(_allele2);
        assertEquals(addedContributor, foundContributor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetContributorAlleleNonexistant() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        hypo.addContributor(_sample1, 0);
        hypo.getContributor(_allele2);
    }

    @Test
    public void testToString() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertEquals(", DropIn 0.00, Theta 0.00", hypo.toString());
        hypo.setUnknownCount(1);
        assertEquals("1 unknown (0.00), DropIn 0.00, Theta 0.00", hypo.toString());
        hypo.setUnknownCount(0);
        hypo.addContributor(_sample1, 0.01);
        assertEquals("TestSample1(0.01), DropIn 0.00, Theta 0.00", hypo.toString());
        hypo.setUnknownCount(1);
        assertEquals("TestSample1(0.01) and 1 unknown (0.00), DropIn 0.00, Theta 0.00", hypo.toString());
        hypo.setUnknownCount(2);
        assertEquals("TestSample1(0.01) and 2 unknowns (0.00), DropIn 0.00, Theta 0.00", hypo.toString());
        hypo.setThetaCorrection(0.01);
        assertEquals("TestSample1(0.01) and 2 unknowns (0.00), DropIn 0.00, Theta 0.01", hypo.toString());
        hypo.setUnknownDropoutProbability(0.02);
        assertEquals("TestSample1(0.01) and 2 unknowns (0.02), DropIn 0.00, Theta 0.01", hypo.toString());
        hypo.setDropInProbability(0.03);
        assertEquals("TestSample1(0.01) and 2 unknowns (0.02), DropIn 0.03, Theta 0.01", hypo.toString());
        hypo.addContributor(_sample2, 0.04);
        assertEquals("TestSample1(0.01), TestSample2(0.04) and 2 unknowns (0.02), DropIn 0.03, Theta 0.01", hypo.toString());
        hypo.removeContributor(_sample2);
        assertEquals("TestSample1(0.01) and 2 unknowns (0.02), DropIn 0.03, Theta 0.01", hypo.toString());
    }

    @Test
    public void testIsContributor() {
        final Hypothesis hypo = new HypothesisImpl(TEST_HYPOTHESIS_ID);
        assertFalse(hypo.isContributor(_sample1));
        hypo.addContributor(_sample1, 0);
        assertTrue(hypo.isContributor(_sample1));
        assertFalse(hypo.isContributor(_sample1a));
        assertFalse(hypo.isContributor(_sample1b));
        assertFalse(hypo.isContributor(_sample2));
        assertFalse(hypo.isContributor(_sample2a));
        hypo.removeContributor(_sample1);
        assertFalse(hypo.isContributor(_sample1));
        assertFalse(hypo.isContributor(_sample1a));
        assertFalse(hypo.isContributor(_sample1b));
        assertFalse(hypo.isContributor(_sample2));
        assertFalse(hypo.isContributor(_sample2a));
    }

    private static class HypothesisImpl extends Hypothesis {

        public HypothesisImpl(final String id) {
            super(id);
        }

        @Override
        public void setCandidate(final Sample candidateSample) {
            fail("This method should not be called. Only the concrete methods should be tested!");
        }

        @Override
        public Hypothesis copy() {
            fail("This method should not be called. Only the concrete methods should be tested!");
            return null;
        }

        @Override
        public Boolean hasCandidate() {
            fail("This method should not be called. Only the concrete methods should be tested!");
            return null;
        }

        @Override
        public Sample getCandidate() {
            fail("This method should not be called. Only the concrete methods should be tested!");
            return null;
        }

        @Override
        public boolean isQDesignationShutdown() {
            fail("This method should not be called. Only the concrete methods should be tested!");
            return false;
        }
    }
}
