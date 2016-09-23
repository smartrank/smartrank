package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SampleTest {

    private static final String LOCUS_NAME = "Some Locus";

    private static final String LOCUS_NAME_1 = LOCUS_NAME + "1";
    private static final String LOCUS_NAME_2 = LOCUS_NAME + "2";

    @Mock
    private Locus _locus1;

    @Mock
    private Locus _locus2;

    @Mock
    private Locus _locus3;

    @Before
    public void setUp() throws Exception {
        when(_locus1.getName()).thenReturn(LOCUS_NAME_1);
        when(_locus2.getName()).thenReturn(LOCUS_NAME_2);
        when(_locus2.getName()).thenReturn(LOCUS_NAME + "3");
    }

    @Test
    public final void testSampleStringString() {
        final Sample sample = new Sample("SampleName", "SourceFileName");
        assertEquals("SampleName", sample.getName());
        assertEquals("SourceFileName", sample.getSourceFile());
        assertNull(sample.getSourceFileHash());
        final Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);
        assertTrue(loci.isEmpty());
    }

    @Test
    public final void testSampleString() {
        final Sample sample = new Sample("SampleName");
        assertEquals("SampleName", sample.getName());
        assertNull(sample.getSourceFile());
        assertNull(sample.getSourceFileHash());
        final Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);
        assertTrue(loci.isEmpty());
    }

    @Test
    public final void testSize() {
        final Sample sample = new Sample("SampleName");
        assertEquals(0, sample.size());
        sample.addLocus(_locus1);
        assertEquals(1, sample.size());
        sample.addLocus(_locus1);
        assertEquals(1, sample.size());
    }

    @Test
    public final void testGetLocus() {
        final Sample sample = new Sample("SampleName");
        assertNull(sample.getLocus(LOCUS_NAME_1));
        sample.addLocus(_locus1);
        assertNull(sample.getLocus(LOCUS_NAME_2));
        assertEquals(_locus1, sample.getLocus(LOCUS_NAME_1));
        sample.addLocus(_locus2);
        assertEquals(_locus1, sample.getLocus(LOCUS_NAME_1));
        assertNull(sample.getLocus("Some Other Locus"));
    }

    @Test
    public final void testHasLocus() {
        final Sample sample = new Sample("SampleName");
        assertFalse(sample.hasLocus(LOCUS_NAME_1));
        sample.addLocus(_locus1);
        assertFalse(sample.hasLocus(LOCUS_NAME_2));
        sample.addLocus(_locus2);
        sample.addLocus(_locus3);
        assertTrue(sample.hasLocus(LOCUS_NAME_1));
        assertFalse(sample.hasLocus("Some Other Locus"));
    }

    @Test
    public final void testToString() {
        final Sample sample = new Sample("SampleName");
        assertNotNull(sample.toString());
    }

    @Test
    public final void testEnabled() {
        final Sample sample = new Sample("SampleName");
        assertTrue(sample.isEnabled());
        sample.setEnabled(false);
        assertFalse(sample.isEnabled());
        sample.setEnabled(true);
        assertTrue(sample.isEnabled());
    }

    @Test
    public final void testSourceFileHash() {
        final Sample sample = new Sample("SampleName");
        assertNull(sample.getSourceFileHash());
        sample.setSourceFileHash("some hash");
        assertEquals("some hash", sample.getSourceFileHash());
    }

    @Test
    public final void testName() {
        final Sample sample = new Sample("SampleName");
        assertEquals("SampleName", sample.getName());
        sample.setName("OtherName");
        assertEquals("OtherName", sample.getName());
    }

    @Test
    public final void testRemoveLocus() {
        final Sample sample = new Sample("SampleName");
        assertEquals(0, sample.getLoci().size());
        sample.addLocus(_locus1);
        assertEquals(1, sample.getLoci().size());
        sample.removeLocus(_locus1);
        assertEquals(0, sample.getLoci().size());
    }

    @Test
    public final void testGetLoci() {
        final Sample sample = new Sample("SampleName");
        final Collection<Locus> loci = sample.getLoci();
        assertNotNull(loci);
        assertTrue(loci.isEmpty());
        sample.addLocus(_locus1);
        final Collection<Locus> loci2 = sample.getLoci();
        assertNotNull(loci2);
        assertEquals(1, loci2.size());
        assertTrue(loci2.contains(_locus1));
    }

}
