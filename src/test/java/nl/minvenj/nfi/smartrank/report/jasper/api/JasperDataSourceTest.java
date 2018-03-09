package nl.minvenj.nfi.smartrank.report.jasper.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Ratio;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.SearchResultsMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

@RunWith(MockitoJUnitRunner.class)
public class JasperDataSourceTest {

    @Mock
    private Sample _crimesceneProfile1;

    @Mock
    private Sample _crimesceneProfile2;

    @Mock
    private Sample _crimesceneProfile3;

    @Mock
    private DefenseHypothesis _hd;

    @Mock
    private ProsecutionHypothesis _hp;

    @Mock
    private PopulationStatistics _stats;

    @Mock
    private LikelihoodRatio _lr1;

    @Mock
    private LikelihoodRatio _lr2;

    @Mock
    private SearchResults _results;

    @Mock
    private AnalysisParameters _parms;

    @Mock
    private DNADatabase _db;

    @Before
    public void setUp() throws Exception {
        when(_crimesceneProfile1.getName()).thenReturn("CrimesceneProfileRep1");
        when(_crimesceneProfile2.getName()).thenReturn("CrimesceneProfileRep2");
        when(_crimesceneProfile3.getName()).thenReturn("AnotherProfile");
        when(_lr1.getOverallRatio()).thenReturn(new Ratio("Locus1", 0.1, 0.05));
        when(_lr2.getOverallRatio()).thenReturn(new Ratio("Locus1", 0.3, 0.35));
        when(_stats.getFileName()).thenReturn("Placeholder for Statistics Filename");
        when(_results.getPositiveLRs()).thenReturn(Arrays.asList(_lr2, _lr1));
        when(_results.getParameters()).thenReturn(_parms);
        when(_parms.getLrThreshold()).thenReturn(1000);

        MessageBus.getInstance().send("JasperDataSourceTest", new SearchResultsMessage(_results));
        MessageBus.getInstance().send("JasperDataSourceTest", new CrimeSceneProfilesMessage(Arrays.asList(_crimesceneProfile1, _crimesceneProfile2, _crimesceneProfile3)));
        MessageBus.getInstance().send("JasperDataSourceTest", new KnownProfilesMessage(new ArrayList<Sample>()));
        MessageBus.getInstance().send("JasperDataSourceTest", new DefenseHypothesisMessage(_hd));
        MessageBus.getInstance().send("JasperDataSourceTest", new ProsecutionHypothesisMessage(_hp));
        MessageBus.getInstance().send("JasperDataSourceTest", new PopulationStatisticsMessage(_stats));
        MessageBus.getInstance().send("JasperDataSourceTest", new AnalysisParametersMessage(_parms));
        MessageBus.getInstance().send("JasperDataSourceTest", new DatabaseMessage(_db));
    }

    @Test
    public final void testNext() throws JRException {
        final JasperDataSource s = new JasperDataSource();
        assertTrue(s.next());
        assertFalse(s.next());
    }

    @Test
    public final void testGetFieldValue() throws JRException {
        final JasperDataSource s = new JasperDataSource();
        final JRField[] fields = JasperDataSource.getFields();
        for (final JRField field : fields) {
            try {
                s.getFieldValue(field);
            }
            catch (final Exception e) {
                System.out.println(field.getName());
                e.printStackTrace();
            }
        }
    }

    @Test
    public final void testGetFields() {
        final JRField[] fields = JasperDataSource.getFields();
        assertNotNull(fields);
        assertTrue(fields.length > 0);
    }

}
