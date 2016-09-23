package nl.minvenj.nfi.smartrank.report.jasper.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.Sample;

@RunWith(MockitoJUnitRunner.class)
public class ResultTest {

    @Mock
    private Sample _sample1;

    @Mock
    private Locus _locus1;

    @Mock
    private Locus _locus2;

    @Before
    public void setUp() throws Exception {
        when(_sample1.getName()).thenReturn("SomeName");
        when(_sample1.getLoci()).thenReturn(Arrays.asList(_locus1, _locus2));
    }

    @Test
    public final void testResult() {
        final JasperDataSource.Result result = new JasperDataSource.Result(1, _sample1, 1, 2.5, "Some comments");
        assertEquals(1, result.getRank());
        assertEquals(_sample1.getName(), result.getProfile());
        assertEquals(2.5, result.getLr(), 0.00000001);
        assertEquals(1, result.getEvaluatedLoci());
        assertEquals("Some comments", result.getComments());
        assertEquals(2, result.getLocusCount());
    }
}
