package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimation;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisParametersTest {

    @Mock
    private Sample _enabledCrimesceneProfile1;

    @Mock
    private Sample _enabledCrimesceneProfile2;

    @Mock
    private DropoutEstimation _dropoutEstimation;

    @Mock
    private Sample _disabledCrimesceneProfile;

    @Before
    public void setUp() throws Exception {
        when(_enabledCrimesceneProfile1.isEnabled()).thenReturn(true);
        when(_enabledCrimesceneProfile1.getName()).thenReturn("Enabled Crimescene Profile 1");

        when(_enabledCrimesceneProfile2.isEnabled()).thenReturn(true);
        when(_enabledCrimesceneProfile2.getName()).thenReturn("Enabled Crimescene Profile 2");

        when(_disabledCrimesceneProfile.isEnabled()).thenReturn(false);
        when(_disabledCrimesceneProfile.getName()).thenReturn("Disabled Crimescene Profile");
    }

    @Test
    public final void testAnalysisParameters() {
        final AnalysisParameters params = new AnalysisParameters();
        final Collection<Sample> enabledCrimesceneProfiles = params.getEnabledCrimesceneProfiles();
        assertNotNull(enabledCrimesceneProfiles);
        assertTrue(enabledCrimesceneProfiles.isEmpty());
    }

    @Test
    public final void testLrThreshold() {
        final AnalysisParameters params = new AnalysisParameters();
        assertEquals(0, params.getLrThreshold());
        params.setLrThreshold(1234);
        assertEquals(1234, params.getLrThreshold());
    }

    @Test
    public final void testThreadCount() {
        final AnalysisParameters params = new AnalysisParameters();
        assertEquals(0, params.getThreadCount());
        params.setThreadCount(1234);
        assertEquals(1234, params.getThreadCount());
    }

    @Test
    public final void testEnabledCrimesceneProfiles() {
        final AnalysisParameters params = new AnalysisParameters();
        final Collection<Sample> enabledCrimesceneProfiles = params.getEnabledCrimesceneProfiles();
        assertNotNull(enabledCrimesceneProfiles);
        assertTrue(enabledCrimesceneProfiles.isEmpty());

        params.setEnabledCrimesceneProfiles(Arrays.asList(_enabledCrimesceneProfile1));
        final Collection<Sample> enabledCrimesceneProfiles1 = params.getEnabledCrimesceneProfiles();
        assertNotNull(enabledCrimesceneProfiles1);
        assertFalse(enabledCrimesceneProfiles1.isEmpty());
        assertEquals(_enabledCrimesceneProfile1, enabledCrimesceneProfiles1.iterator().next());

        params.setEnabledCrimesceneProfiles(Arrays.asList(_disabledCrimesceneProfile, _enabledCrimesceneProfile2));
        final Collection<Sample> enabledCrimesceneProfiles2 = params.getEnabledCrimesceneProfiles();
        assertNotNull(enabledCrimesceneProfiles2);
        assertFalse(enabledCrimesceneProfiles2.isEmpty());
        assertEquals(_enabledCrimesceneProfile2, enabledCrimesceneProfiles2.iterator().next());
    }

    @Test
    public final void testGetDropoutEstimation() {
        final AnalysisParameters params = new AnalysisParameters();
        assertNull(params.getDropoutEstimation());
        params.setDropoutEstimation(_dropoutEstimation);
        assertEquals(_dropoutEstimation, params.getDropoutEstimation());
    }
}
