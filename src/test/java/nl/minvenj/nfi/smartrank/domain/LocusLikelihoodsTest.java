package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class LocusLikelihoodsTest {

    private static final Double LOCUS_PROBABILITY_1 = 0.11;
    private static final Double LOCUS_PROBABILITY_2 = 0.22;
    private static final Double LOCUS_PROBABILITY_3 = 0.33;
    private static final String LOCUS_NAME_1 = "LocusName1";
    private static final String LOCUS_NAME_2 = "LocusName2";
    private static final String LOCUS_NAME_3 = "LocusName3";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testLocusProbability() {
        final LocusLikelihoods loli = new LocusLikelihoods();
        loli.addLocusProbability(LOCUS_NAME_1, LOCUS_PROBABILITY_1);
        assertEquals(LOCUS_PROBABILITY_1, loli.getLocusProbability(LOCUS_NAME_1));
    }

    @Test
    public final void testGetLoci() {
        final LocusLikelihoods loli = new LocusLikelihoods();
        final Collection<String> loci1 = loli.getLoci();
        assertNotNull(loci1);
        assertTrue(loci1.isEmpty());
        loli.addLocusProbability(LOCUS_NAME_1, LOCUS_PROBABILITY_1);
        final Collection<String> loci2 = loli.getLoci();
        assertNotNull(loci2);
        assertEquals(1, loci2.size());
        assertTrue(loci2.contains(LOCUS_NAME_1));
        loli.addLocusProbability(LOCUS_NAME_2, LOCUS_PROBABILITY_2);
        final Collection<String> loci3 = loli.getLoci();
        assertNotNull(loci3);
        assertEquals(2, loci3.size());
        assertTrue(loci3.containsAll(Arrays.asList(LOCUS_NAME_1, LOCUS_NAME_2)));
    }

    @Test
    public final void testGetGlobalProbability() {
        final LocusLikelihoods loli = new LocusLikelihoods();
        assertEquals(1, loli.getGlobalProbability(), 0.00000001);
        loli.addLocusProbability(LOCUS_NAME_1, LOCUS_PROBABILITY_1);
        assertEquals(LOCUS_PROBABILITY_1, loli.getGlobalProbability(), 0.00000001);
        loli.addLocusProbability(LOCUS_NAME_2, LOCUS_PROBABILITY_2);
        assertEquals(LOCUS_PROBABILITY_1 * LOCUS_PROBABILITY_2, loli.getGlobalProbability(), 0.00000001);
        loli.addLocusProbability(LOCUS_NAME_3, LOCUS_PROBABILITY_3);
        assertEquals(LOCUS_PROBABILITY_1 * LOCUS_PROBABILITY_2 * LOCUS_PROBABILITY_3, loli.getGlobalProbability(), 0.00000001);
    }

    @Test
    public final void testToString() {
        final LocusLikelihoods loli = new LocusLikelihoods();
        assertEquals("{}", loli.toString());
        loli.addLocusProbability(LOCUS_NAME_1, LOCUS_PROBABILITY_1);
        assertTrue(loli.toString().contains(LOCUS_NAME_1 + "=" + LOCUS_PROBABILITY_1));
        loli.addLocusProbability(LOCUS_NAME_2, LOCUS_PROBABILITY_2);
        assertTrue(loli.toString().contains(LOCUS_NAME_1 + "=" + LOCUS_PROBABILITY_1));
        assertTrue(loli.toString().contains(LOCUS_NAME_2 + "=" + LOCUS_PROBABILITY_2));
        loli.addLocusProbability(LOCUS_NAME_3, LOCUS_PROBABILITY_3);
        assertTrue(loli.toString().contains(LOCUS_NAME_1 + "=" + LOCUS_PROBABILITY_1));
        assertTrue(loli.toString().contains(LOCUS_NAME_2 + "=" + LOCUS_PROBABILITY_2));
        assertTrue(loli.toString().contains(LOCUS_NAME_3 + "=" + LOCUS_PROBABILITY_3));
    }

}
