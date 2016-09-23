package nl.minvenj.nfi.smartrank.model.smartrank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.model.StatisticalModel;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

@RunWith(MockitoJUnitRunner.class)
public class SmartRankModelTest {

    private static final String UNUSED_LOCUS_NAME = "UnusedLocus";
    private static final String LOCUS_NAME = "SOMELOCUS";
    private static final String ANOTHER_UNUSED_LOCUS_NAME = "AnotherUnusedLocus";
    private AnalysisParameters _parameters;
    private ProsecutionHypothesis _hp;
    private DefenseHypothesis _hd;
    private Sample _candidate;
    private Sample _candidate2;
    private Sample _noncontributor;

    @Before
    public void setUp() throws Exception {
        final PopulationStatistics stats = new PopulationStatistics("");
        stats.addStatistic(LOCUS_NAME, "1", new BigDecimal(0.01));
        stats.addStatistic(LOCUS_NAME, "2", new BigDecimal(0.02));
        stats.addStatistic(LOCUS_NAME, "3", new BigDecimal(0.03));

        final Sample replicate = setupSample("replicate", LOCUS_NAME, "1", "2", "3");
        _candidate = setupSample("candidate1", LOCUS_NAME, "1", "4");
        _candidate2 = setupSample("candidate2", LOCUS_NAME, "1", "2");
        _noncontributor = setupSample("noncontributor", LOCUS_NAME, "2", "2");

        _hp = new ProsecutionHypothesis();
        _hp.setStatistics(stats);
        _hp.setDropInProbability(0.05);
        _hp.setUnknownCount(1);
        _hp.setCandidate(_candidate);
        _hp.getContributor(_candidate).setDropoutProbability(0.04);

        _hd = new DefenseHypothesis();
        _hd.setStatistics(stats);
        _hd.setDropInProbability(0.05);
        _hd.setUnknownCount(2);
        _hd.setUnknownDropoutProbability(0.06);

        _parameters = new AnalysisParameters();
        _parameters.setEnabledCrimesceneProfiles(Arrays.asList(replicate));

        // We are calling the locus contructor to update the numberOfRegisteredLoci counter in Locus.
        final Locus unusedLocus = new Locus(UNUSED_LOCUS_NAME); // Create a new locus in order to generate an Id for it.
        MessageBus.getInstance().send("SmartRankModelTest", new EnabledLociMessage(Arrays.asList(LOCUS_NAME, UNUSED_LOCUS_NAME)));
    }

    private Sample setupSample(final String sampleName, final String locusName, final String... alleles) {
        final Sample sample = new Sample(sampleName);
        final Locus locus = new Locus(locusName);
        for (final String a : alleles) {
            locus.addAllele(new Allele(a));
        }
        sample.addLocus(locus);
        final Locus unusedLocus = new Locus(ANOTHER_UNUSED_LOCUS_NAME);
        unusedLocus.addAllele(new Allele("1"));
        unusedLocus.addAllele(new Allele("2"));
        sample.addLocus(unusedLocus);
        return sample;
    }

    @Test
    public final void testSmartRankModel() {
        final SmartRankModel model = new SmartRankModel();
        assertEquals("SmartRankModel", model.getModelName());
        assertTrue(model instanceof StatisticalModel);
    }

    @Test
    public final void testCalculateLikelihoodHpTheta001() throws InterruptedException {
        final SmartRankModel model = new SmartRankModel();
        _hp.setThetaCorrection(0.1);
        final LocusLikelihoods calculatedLikelihood = model.calculateLikelihood(_hp, _parameters);
        assertEquals(2.5813877875812905E-5, calculatedLikelihood.getGlobalProbability(), 0.0000000000000001);

        final LocusLikelihoods calculatedLikelihood2 = model.calculateLikelihood(_hp, _parameters);
        assertEquals(2.5813877875812905E-5, calculatedLikelihood2.getGlobalProbability(), 0.0000000000000001);

        _hp.setCandidate(_candidate2);
        final LocusLikelihoods calculatedLikelihood3 = model.calculateLikelihood(_hp, _parameters);
        assertEquals(0.010559620221097455, calculatedLikelihood3.getGlobalProbability(), 0.0000000000000001);
    }

    @Test
    public final void testCalculateLikelihoodHpTheta0() throws InterruptedException {
        final SmartRankModel model = new SmartRankModel();
        _hp.setThetaCorrection(0);
        final LocusLikelihoods calculatedLikelihood = model.calculateLikelihood(_hp, _parameters);
        assertEquals(4.1292896687783985E-5, calculatedLikelihood.getGlobalProbability(), 0.0000000000000001);
    }

    @Test
    public final void testCalculateLikelihoodHdTheta001() throws InterruptedException {
        final SmartRankModel model = new SmartRankModel();
        _hd.setThetaCorrection(0.01);
        final LocusLikelihoods calculatedLikelihood = model.calculateLikelihood(_hd, _parameters);
        assertEquals(5.134456249927388E-6, calculatedLikelihood.getGlobalProbability(), 0.0000000000000001);

        _hd.addNonContributor(_noncontributor, 0);
        final LocusLikelihoods calculatedLikelihood2 = model.calculateLikelihood(_hd, _parameters);
        assertEquals(1.1674250396829341E-5, calculatedLikelihood2.getGlobalProbability(), 0.0000000000000001);
    }

    @Test
    public final void testCalculateLikelihoodHdTheta0() throws InterruptedException {
        final SmartRankModel model = new SmartRankModel();
        _hd.setThetaCorrection(0);
        final LocusLikelihoods calculatedLikelihood = model.calculateLikelihood(_hd, _parameters);
        assertEquals(3.6183637675994007E-6, calculatedLikelihood.getGlobalProbability(), 0.0000000000000001);

        _hd.addNonContributor(_noncontributor, 0);
        final LocusLikelihoods calculatedLikelihood2 = model.calculateLikelihood(_hd, _parameters);
        assertEquals(calculatedLikelihood.getGlobalProbability(), calculatedLikelihood2.getGlobalProbability(), 0.0000000000000001);
    }

    @Test
    public final void testInterrupt() throws InterruptedException {
        final SmartRankModel model = new SmartRankModel();
        model.calculateLikelihood(_hd, _parameters);
        model.interrupt();
    }

    @Test
    public final void testReset() {
        final SmartRankModel model = new SmartRankModel();
        model.reset();
    }
}
