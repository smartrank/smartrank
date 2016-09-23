package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class RatioTest {

    private static final Double PRP = 0.01;
    private static final Double PRD = 0.02;
    private static final String LOCUS_NAME = "LocusName";
    private static final Double RATIO = 0.03;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.US);
    }

    @Test
    public final void testRatioStringDoubleDouble() {
        final Ratio ratio = new Ratio(LOCUS_NAME, PRP, PRD);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertEquals(PRD, ratio.getDefenseProbability(), 0.000000001);
        assertEquals(PRP, ratio.getProsecutionProbability(), 0.000000001);
        assertEquals((0.01 / 0.02), ratio.getRatio(), 0.000000001);
        assertEquals("5.0000E-01", ratio.toString());
    }

    @Test
    public final void testRatioStringDoubleNull() {
        final Ratio ratio = new Ratio(LOCUS_NAME, PRP, null);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertNull(ratio.getDefenseProbability());
        assertEquals(PRP, ratio.getProsecutionProbability(), 0.000000001);
        assertEquals(Double.NaN, ratio.getRatio(), 0.000000001);
        assertEquals("NAN", ratio.toString());
    }

    @Test
    public final void testRatioStringNullDouble() {
        final Ratio ratio = new Ratio(LOCUS_NAME, null, PRD);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertEquals(PRD, ratio.getDefenseProbability(), 0.000000001);
        assertNull(ratio.getProsecutionProbability());
        assertEquals(Double.NaN, ratio.getRatio(), 0.000000001);
        assertEquals("NAN", ratio.toString());
    }

    @Test
    public final void testRatioStringNullDoubleDouble() {
        final Ratio ratio = new Ratio(LOCUS_NAME, null, PRD, RATIO);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertEquals(PRD, ratio.getDefenseProbability(), 0.000000001);
        assertNull(ratio.getProsecutionProbability());
        assertEquals(Double.NaN, ratio.getRatio(), 0.000000001);
        assertEquals("NAN", ratio.toString());
    }

    @Test
    public final void testRatioStringDoubleNullDouble() {
        final Ratio ratio = new Ratio(LOCUS_NAME, PRP, null, RATIO);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertNull(ratio.getDefenseProbability());
        assertEquals(PRP, ratio.getProsecutionProbability(), 0.000000001);
        assertEquals(Double.NaN, ratio.getRatio(), 0.000000001);
        assertEquals("NAN", ratio.toString());
    }

    @Test
    public final void testRatioStringDoubleDoubleNull() {
        final Ratio ratio = new Ratio(LOCUS_NAME, PRP, PRD, null);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertEquals(PRD, ratio.getDefenseProbability(), 0.000000001);
        assertEquals(PRP, ratio.getProsecutionProbability(), 0.000000001);
        assertEquals((0.01 / 0.02), ratio.getRatio(), 0.000000001);
        assertEquals("5.0000E-01", ratio.toString());
    }

    @Test
    public final void testRatioStringDoubleDoubleDouble() {
        final Ratio ratio = new Ratio(LOCUS_NAME, PRP, PRD, RATIO);

        assertEquals(LOCUS_NAME, ratio.getLocusName());
        assertEquals(PRD, ratio.getDefenseProbability(), 0.000000001);
        assertEquals(PRP, ratio.getProsecutionProbability(), 0.000000001);
        assertEquals(0.03, ratio.getRatio(), 0.000000001);
        assertEquals("3.0000E-02", ratio.toString());
    }
}
