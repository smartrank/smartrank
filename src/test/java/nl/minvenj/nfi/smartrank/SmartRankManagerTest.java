package nl.minvenj.nfi.smartrank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class SmartRankManagerTest {

    private SmartRankManager _manager;

    @Before
    public void setUp() throws Exception {
        MessageBus.reset();
        SmartRankManager.reset();
        _manager = SmartRankManager.getInstance();
    }

    @Test
    public final void testSmartRankManager() {
        assertNotNull(MessageBus.getInstance().query(AnalysisParametersMessage.class));
        assertEquals(ApplicationStatus.WAIT_DB, MessageBus.getInstance().query(ApplicationStatusMessage.class));
    }

    @Test
    @Ignore
    public final void testOnNewDatabase() {
        _manager.onNewDatabase(getTestFile("database-5002.csv"));
        final DNADatabase db = MessageBus.getInstance().query(DatabaseMessage.class);
        assertNotNull(db);
        assertTrue(db.getConnectString().endsWith("database-5002.csv"));
    }

    @Test
    public final void testOnNewDatabaseNull() {
        _manager.onNewDatabase(null);
    }

    @Test
    @Ignore
    public final void testOnLoadCrimesceneProfiles() throws InterruptedException {
        MessageBus.getInstance().send("SmartRankManagerTest", new CrimeSceneProfilesMessage(Collections.<Sample>emptyList()));
        _manager.onLoadCrimesceneProfiles(new File[]{getTestFile("sample.csv")});
        final Collection<Sample> profiles = MessageBus.getInstance().query(CrimeSceneProfilesMessage.class);
        assertNotNull(profiles);
        for (final Sample profile : profiles) {
            assertTrue("Unexpected sample name: " + profile.getName(), profile.getName().matches("AAFV9069NL#01Rep\\d"));
        }
    }

    private File getTestFile(final String name) {
        final URL url = getClass().getResource(name);
        return new File(url.getPath());
    }
/*
    @Test
    public final void testOnRemoveCrimesceneProfile() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnCrimesceneProfileUpdated() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnLoadKnownProfiles() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnSaveReport() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateRareAlleleFrequency() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnRemoveKnownProfiles() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnKnownProfileUpdated() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnStartAnalysis() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnNewPopulationStatistics() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnEstimateDropout() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnStopAnalysis() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateDefenseContributor() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateDefenseNonContributor() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateProsecutionContributor() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateProsecutionNonContributor() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateProsecutionUnknowns() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateDefenseUnknowns() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateLRThreshold() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnReportSaved() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateDropin() {
        fail("Not yet implemented");
    }

    @Test
    public final void testOnUpdateTheta() {
        fail("Not yet implemented");
    }
*/
}
