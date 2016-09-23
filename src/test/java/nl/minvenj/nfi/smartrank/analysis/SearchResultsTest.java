package nl.minvenj.nfi.smartrank.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.LocusLikelihoods;
import nl.minvenj.nfi.smartrank.domain.Sample;

public class SearchResultsTest {

    private LikelihoodRatio _lr1;
    private LikelihoodRatio _lr2;
    private LikelihoodRatio _lr3;
    private LocusLikelihoods _prD1;
    private LocusLikelihoods _prD2;
    private LocusLikelihoods _prD3;
    private LocusLikelihoods _prP1;
    private LocusLikelihoods _prP2;
    private LocusLikelihoods _prP3;
    private Sample _sample1;
    private Sample _sample2;
    private Sample _sample3;
    private Locus _locus1;
    private Locus _locus2;
    private Locus _locus3;

    @Before
    public void setUp() throws Exception {
        _sample1 = new Sample("DummySample1");
        _sample2 = new Sample("DummySample2");
        _sample3 = new Sample("DummySample3");

        _locus1 = new Locus("Locus1");
        _locus2 = new Locus("Locus2");
        _locus3 = new Locus("Locus3");

        _sample1.addLocus(_locus1);

        _sample2.addLocus(_locus1);
        _sample2.addLocus(_locus2);

        _sample3.addLocus(_locus1);
        _sample3.addLocus(_locus2);
        _sample3.addLocus(_locus3);

        _prP1 = new LocusLikelihoods();
        _prP1.addLocusProbability("Locus1", 0.6);
        _prD1 = new LocusLikelihoods();
        _prD1.addLocusProbability("Locus1", 0.5);

        _prP2 = new LocusLikelihoods();
        _prP2.addLocusProbability("Locus2", 0.4);
        _prD2 = new LocusLikelihoods();
        _prD2.addLocusProbability("Locus2", 0.5);

        _prP3 = new LocusLikelihoods();
        _prP3.addLocusProbability("Locus1", 0.99);
        _prP3.addLocusProbability("Locus2", 0.6);
        _prD3 = new LocusLikelihoods();
        _prD3.addLocusProbability("Locus1", 0.07);
        _prD3.addLocusProbability("Locus2", 0.005);

        _lr1 = new LikelihoodRatio(_sample1, _prP1, _prD1);
        _lr2 = new LikelihoodRatio(_sample2, _prP2, _prD2);
        _lr3 = new LikelihoodRatio(_sample3, _prP3, _prD3);
    }

    @Test
    public final void testSearchResults() {
        final SearchResults results = new SearchResults(0);
        assertEquals(0, results.getNumberOfLRs());
    }

    @Test
    public final void testGetNumberOfLRsOver1() {
        final SearchResults results = new SearchResults(10);

        assertEquals(0, results.getNumberOfLRs());
        assertEquals(0, results.getNumberOfLRsOver1());

        results.addLR(_lr1);

        assertEquals(1, results.getNumberOfLRs());
        assertEquals(1, results.getNumberOfLRsOver1());

        results.addLR(_lr2);
        assertEquals(2, results.getNumberOfLRs());
        assertEquals(1, results.getNumberOfLRsOver1());
    }

    @Test
    public final void testGetMaxRatio() {
        final SearchResults results = new SearchResults(3);

        results.addLR(_lr1);
        results.addLR(_lr2);
        results.addLR(_lr3);
        assertEquals(1697.1428571428569, results.getMaxRatio(), 0.00000001);
    }

    @Test
    public final void testGetMinRatio() {
        final SearchResults results = new SearchResults(3);
        results.addLR(_lr1);
        results.addLR(_lr2);
        results.addLR(_lr3);
        assertEquals(0.8, results.getMinRatio(), 0.00000001);
    }

    @Test
    public final void testDuration() {
        final SearchResults results = new SearchResults(0);
        assertEquals(0, results.getDuration());
        results.setDuration(1234);
        assertEquals(1234, results.getDuration());
    }

    @Test
    public final void testGetResultsPerNumberOfLoci() {
        final SearchResults results = new SearchResults(3);
        results.addLR(_lr1);
        results.addLR(_lr2);
        results.addLR(_lr3);
        final Collection<ResultsPerLocus> resultsPerNumberOfLoci = results.getResultsPerNumberOfLoci(1);
        assertNotNull(resultsPerNumberOfLoci);
        assertEquals(2, resultsPerNumberOfLoci.size());
    }

    @Test
    public final void testGetPercentile() {
        final SearchResults results = new SearchResults(3);
        results.addLR(_lr1);
        results.addLR(_lr2);
        results.addLR(_lr3);
        assertEquals(0.8, results.getPercentile(1), 0.00000001);
        assertEquals(0.8, results.getPercentile(10), 0.00000001);
        assertEquals(1.2, results.getPercentile(50), 0.00000001);
        assertEquals(1357.954285714285, results.getPercentile(70), 0.00000001);
        assertEquals(1697.1428571428569, results.getPercentile(99), 0.00000001);
    }

}
