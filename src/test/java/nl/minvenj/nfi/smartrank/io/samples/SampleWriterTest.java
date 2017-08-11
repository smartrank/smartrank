package nl.minvenj.nfi.smartrank.io.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class SampleWriterTest {

    private Sample _sample;

    @Before
    public void setUp() throws Exception {
        final URL resource = getClass().getResource("writerInput.csv");
        assertNotNull("Test file 'writerInput.csv' could not be found!", resource);

        SampleReader sampleReader = null;
        try {
            sampleReader = new SampleReader(new File(resource.toURI()));
        }
        catch (final Throwable t) {
            t.printStackTrace();
            fail("Test file 'writerInput.csv' could not be read!");
        }

        final Collection<Sample> samples = sampleReader.getSamples();
        assertFalse("Test file 'writerInput.csv' did not contain sample data!", samples.isEmpty());

        _sample = samples.iterator().next();
    }

    @Test
    public final void testWriteOnce() throws IOException {
        final File outFile = new File(new File(_sample.getSourceFile()).getParent(), "writerOutput.csv");

        final SampleWriter writer = new SampleWriter(outFile);
        writer.write(_sample);

        final Collection<Sample> samples = new SampleReader(outFile).getSamples();
        assertNotNull("Sample collection is null!", samples);
        assertEquals("Expected 1 sample, but got " + samples.size(), 1, samples.size());

        verifySample(samples.iterator().next());
    }

    @Test
    public final void testWriteTwice() throws IOException {

        final SampleWriter writer = new SampleWriter(new File(new File(_sample.getSourceFile()).getParent(), "writerOutput.csv"));
        final File firstOutFile = writer.write(_sample);

        final Collection<Sample> samplesFirst = new SampleReader(firstOutFile).getSamples();
        assertNotNull("Sample collection is null!", samplesFirst);
        assertEquals("Expected 1 sample, buf got " + samplesFirst.size(), 1, samplesFirst.size());

        verifySample(samplesFirst.iterator().next());

        // Write the sample again. We expect a file to be created withthe name 'writerOutput-copy-0.csv'
        final File secondOutFile = writer.write(_sample);

        final Collection<Sample> samplesSecond = new SampleReader(secondOutFile).getSamples();
        assertNotNull("second sample collection is null!", samplesSecond);
        assertEquals("Expected 1 sample in second sample collection, but got " + samplesSecond.size(), 1, samplesSecond.size());

        verifySample(samplesSecond.iterator().next());
    }

    private void verifySample(final Sample actual) {

        // Check if all loci in the actual sample were expected and had the expected alleles
        for (final Locus actualLocus : actual.getLoci()) {
            final Locus expectedLocus = _sample.getLocus(actualLocus.getName());
            assertNotNull("Unexpected locus: " + actualLocus.getName(), expectedLocus);
            for (final Allele actualAllele : actualLocus.getAlleles()) {
                assertTrue("Unexpected allele " + actualAllele.getAllele() + " in locus " + actualLocus.getName(), expectedLocus.hasAllele(actualAllele.getAllele()));
            }
            assertEquals("Expected " + expectedLocus.getAlleles() + " at locus " + actualLocus.getName() + " but got " + actualLocus.getAlleles(), expectedLocus.size(), actualLocus.size());
        }

        // Check for any expected loci that were not present in the actual sample.
        for (final Locus expectedLocus : _sample.getLoci()) {
            final Locus actualLocus = actual.getLocus(expectedLocus.getName());
            assertNotNull("Expected locus not found: " + expectedLocus.getName(), actualLocus);
        }
    }

}
