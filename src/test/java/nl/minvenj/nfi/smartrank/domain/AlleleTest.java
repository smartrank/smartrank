package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlleleTest {

    @Mock
    private Locus _locus;

    @Mock
    private Locus _homozygoteLocus;

    @Before
    public void setUp() throws Exception {
        when(_locus.isHomozygote()).thenReturn(false);
        when(_homozygoteLocus.isHomozygote()).thenReturn(true);
    }

    @Test
    public final void testHashCode() {
        final Allele a1 = new Allele("same");
        final Allele a2 = new Allele("same.0");
        final Allele a3 = new Allele("different");
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1.hashCode(), a3.hashCode());
    }

    @Test
    public final void testGetId() {
        final Allele a = new Allele("1");
        assertEquals(a.getId(), Allele.getId("1"));
        assertNotEquals(a.getId(), Allele.getId("2"));
    }

    @Test
    public final void testGetRegisteredAlleleCount() {
        final int oldAlleleCount = Allele.getRegisteredAlleleCount();
        Allele.getId("testGetRegisteredAlleleCount");
        final int newAlleleCount = Allele.getRegisteredAlleleCount();
        assertTrue(newAlleleCount == (oldAlleleCount + 1));
    }

    @Test
    public final void testNormalize() {
        assertEquals("1", Allele.normalize("1"));
        assertEquals("3", Allele.normalize("3.0"));
        assertEquals("3.4", Allele.normalize("3.4"));
    }

    @Test
    public final void testAlleleString() {
        final Allele a = new Allele("testAlleleString");
        assertEquals(0f, a.getPeak(), 0.00000001);
        assertFalse(a.isHomozygote());
    }

    @Test
    public final void testAlleleStringFloat() {
        final Allele a = new Allele("SomeName", 0.01f);
        assertEquals("SomeName", a.getAllele());
        assertEquals(0.01f, a.getPeak(), 0.0000001);
        assertFalse(a.isHomozygote());
    }

    @Test
    public final void testLocus() {
        final Allele a = new Allele("1");
        assertNull(a.getLocus());
        a.setLocus(_locus);
        assertEquals(_locus, a.getLocus());
    }

    @Test
    public final void testIsHomozygote() {
        final Allele a = new Allele("1");
        assertFalse(a.isHomozygote());
        a.setLocus(_homozygoteLocus);
        assertTrue(a.isHomozygote());
        a.setLocus(_locus);
        assertFalse(a.isHomozygote());
    }

    @Test
    public final void testGetPeak() {
        final Allele allele = new Allele("1", 0.5f);
        assertEquals(0.5f, allele.getPeak(), 0.0000001);
    }

    @Test
    public final void testToString() {
        final Allele allele = new Allele("1");
        assertEquals("1", allele.toString());
        allele.setLocus(_homozygoteLocus);
        assertEquals("1'", allele.toString());
    }

    @Test
    public final void testEqualsObject() {
        final Allele a1 = new Allele("1");
        final Allele a2 = new Allele("2");
        final Allele a3 = new Allele("1");

        assertTrue(a1.equals(a1));
        assertTrue(a1.equals(a3));
        assertFalse(a1.equals(a2));
        assertFalse(a1.equals("Some Arbitrary Object"));
    }

}
