package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LikelihoodRatioTest {

    @Mock
    private Sample _profile;
    @Mock
    private LocusLikelihoods _likelihoods1;
    @Mock
    private LocusLikelihoods _likelihoods2;
    @Mock
    private LocusLikelihoods _likelihoods3;
    @Mock
    private LocusLikelihoods _likelihoods4;
    @Mock
    private Ratio _ratioRatioNull;
    @Mock
    private Ratio _ratioPrDNull;
    @Mock
    private Ratio _ratioPrPNull;

    @Before
    public void setUp() throws Exception {
        when(_likelihoods1.getLoci()).thenReturn(Arrays.asList("Locus1", "Locus2", "Locus3"));
        when(_likelihoods1.getLocusProbability("Locus1")).thenReturn(0.11);
        when(_likelihoods1.getLocusProbability("Locus2")).thenReturn(0.12);
        when(_likelihoods1.getLocusProbability("Locus3")).thenReturn(0.13);
        when(_likelihoods1.getLocusProbability("Locus4")).thenReturn(null);

        when(_likelihoods2.getLoci()).thenReturn(Arrays.asList("Locus2", "Locus3", "Locus4"));
        when(_likelihoods2.getLocusProbability("Locus1")).thenReturn(null);
        when(_likelihoods2.getLocusProbability("Locus2")).thenReturn(0.22);
        when(_likelihoods2.getLocusProbability("Locus3")).thenReturn(0.23);
        when(_likelihoods2.getLocusProbability("Locus4")).thenReturn(0.24);

        when(_likelihoods3.getLoci()).thenReturn(Arrays.asList("Locus5", "Locus6"));
        when(_likelihoods3.getLocusProbability("Locus1")).thenReturn(null);
        when(_likelihoods3.getLocusProbability("Locus2")).thenReturn(null);
        when(_likelihoods3.getLocusProbability("Locus3")).thenReturn(null);
        when(_likelihoods3.getLocusProbability("Locus4")).thenReturn(null);
        when(_likelihoods3.getLocusProbability("Locus5")).thenReturn(0.35);
        when(_likelihoods3.getLocusProbability("Locus6")).thenReturn(0.36);

        when(_likelihoods4.getLoci()).thenReturn(Arrays.asList("Locus6"));
        when(_likelihoods4.getLocusProbability("Locus1")).thenReturn(null);
        when(_likelihoods4.getLocusProbability("Locus2")).thenReturn(null);
        when(_likelihoods4.getLocusProbability("Locus3")).thenReturn(null);
        when(_likelihoods4.getLocusProbability("Locus4")).thenReturn(null);
        when(_likelihoods4.getLocusProbability("Locus5")).thenReturn(null);
        when(_likelihoods4.getLocusProbability("Locus6")).thenReturn(0.46);

        when(_ratioRatioNull.getLocusName()).thenReturn("RatioNull");
        when(_ratioRatioNull.getProsecutionProbability()).thenReturn(0.01);
        when(_ratioRatioNull.getDefenseProbability()).thenReturn(0.02);
        when(_ratioRatioNull.getRatio()).thenReturn(null);

        when(_ratioPrPNull.getLocusName()).thenReturn("PrPNull");
        when(_ratioPrPNull.getDefenseProbability()).thenReturn(0.02);
        when(_ratioPrPNull.getProsecutionProbability()).thenReturn(null);
        when(_ratioPrPNull.getRatio()).thenReturn(1.0);

        when(_ratioPrDNull.getLocusName()).thenReturn("PrDNull");
        when(_ratioPrDNull.getProsecutionProbability()).thenReturn(0.01);
        when(_ratioPrDNull.getDefenseProbability()).thenReturn(null);
        when(_ratioPrDNull.getRatio()).thenReturn(1.0);
    }

    @Test
    public final void testLikelihoodRatio() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        assertEquals(2, lr.getLocusCount());

        final Iterable<String> loci = lr.getLoci();
        assertNotNull(loci);

        int count = 0;
        for (final String locusName : loci) {
            assertTrue(locusName.matches("Locus[23]"));
            count++;
        }
        assertEquals(lr.getLocusCount(), count);
    }

    @Test
    public final void testAdd() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        lr.add(_likelihoods3, _likelihoods4);

        assertEquals(4, lr.getLocusCount());

        final Iterable<String> loci = lr.getLoci();
        assertNotNull(loci);
        int count = 0;
        for (final String locusName : loci) {
            assertTrue("locusName unexpected: " + locusName, locusName.matches("Locus[2356]"));
            count++;
        }
        assertEquals(lr.getLocusCount(), count);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testAddNullNull() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        lr.add(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testAddNonNullNull() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        lr.add(_likelihoods3, null);
    }

    @Test
    public final void testGetRatio() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        final Ratio ratio2 = lr.getRatio("Locus2");
        assertNotNull(ratio2);
        assertEquals("Locus2", ratio2.getLocusName());

        final Ratio ratioNE = lr.getRatio("DoesNotExist");
        assertNull(ratioNE);
    }

    @Test
    public final void testPutRatio() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        assertEquals(2, lr.getLocusCount());
        lr.putRatio(_ratioPrDNull);
        assertEquals(3, lr.getLocusCount());
    }

    @Test
    public final void testGetOverallRatio() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        lr.putRatio(_ratioRatioNull);
        lr.putRatio(_ratioPrDNull);
        lr.putRatio(_ratioPrPNull);
        assertEquals((0.12d / 0.22d) * (0.13d / 0.23d), lr.getOverallRatio().getRatio(), 0.00000001);
    }

    @Test
    public final void testGetRatios() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        final Collection<Ratio> ratios = lr.getRatios();
        assertNotNull(ratios);
        assertEquals(2, ratios.size());
        for (final Ratio ratio : ratios) {
            assertTrue(ratio.getLocusName().matches("Locus[23]"));
        }
    }

    @Test
    public final void testGetProfile() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        assertEquals(_profile, lr.getProfile());
    }

    @Test
    public final void testCompareTo() {
        final LikelihoodRatio lr1 = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        final LikelihoodRatio lr2 = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        final LikelihoodRatio lr3 = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        lr3.add(_likelihoods3, _likelihoods4);
        assertTrue(lr1.compareTo(lr2) == 0);
        assertTrue(lr1.compareTo(lr3) != 0);
    }

    @Test
    public final void testToString() {
        final LikelihoodRatio lr = new LikelihoodRatio(_profile, _likelihoods1, _likelihoods2);
        Locale.setDefault(Locale.US);
        assertEquals("3.0830E-01", lr.toString());
    }

}
