package nl.minvenj.nfi.smartrank.analysis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResultsPerLocusTest {

    @Test
    public final void testResultsPerLocus() {
        final ResultsPerLocus results = new ResultsPerLocus(1, 5);
        assertEquals(1, results.getLocusCount());
        assertEquals(5, results.getSpecimenCount());
    }
}
