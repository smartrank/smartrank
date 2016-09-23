package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PopulationStatisticsTest {

    private static final String FILE_NAME = "StatisticsFileName";
    private static final String FILE_HASH = "StatisticsFileHash";
    private static final String ALLELE_1 = "1";
    private static final String ALLELE_2 = "2";
    private static final String ALLELE_3 = "3";
    private static final String LOCUSNAME_1 = "Locus1";
    private static final String LOCUSNAME_2 = "Locus2";

    @Mock
    private Locus _locus;

    @Mock
    private Allele _allele1;

    @Mock
    private Allele _rareAllele;

    @Before
    public void setUp() throws Exception {
        when(_locus.getName()).thenReturn(LOCUSNAME_1);
        when(_locus.getId()).thenReturn(Locus.getId(LOCUSNAME_1));
        when(_allele1.getAllele()).thenReturn(ALLELE_1);
        when(_allele1.getLocus()).thenReturn(_locus);
        when(_allele1.getId()).thenReturn(Allele.getId(ALLELE_1));
        when(_rareAllele.getAllele()).thenReturn(ALLELE_3);
        when(_rareAllele.getLocus()).thenReturn(_locus);
        when(_rareAllele.getId()).thenReturn(Allele.getId(ALLELE_3));
    }

    @Test
    public final void testHashCode() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        assertEquals(-1170505412, stats.hashCode());
        stats.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.15));
        assertEquals(966780287, stats.hashCode());
        stats.addStatistic(LOCUSNAME_2, ALLELE_2, new BigDecimal(0.012));
        assertEquals(-1496839697, stats.hashCode());
    }

    @Test
    public final void testPopulationStatistics() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        assertNotNull(stats.getLoci());
        assertEquals(0, stats.getLoci().size());
    }

    @Test
    public final void testRareAlleleFrequency() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        assertEquals(0.00023980815347721823, stats.getRareAlleleFrequency(), 0.00000001);
        stats.setRareAlleleFrequency(0.01);
        assertEquals(0.01, stats.getRareAlleleFrequency(), 0.00000001);

        // No statistics added yet. The following should result in the rare allele frequency
        assertEquals(0.01, stats.getProbability(LOCUSNAME_1, ALLELE_1), 0.00000001);
    }

    @Test
    public final void testStatistic() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        stats.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.15));
        stats.addStatistic(LOCUSNAME_1, ALLELE_2, new BigDecimal(0.012));
        final Double probabilityStringString = stats.getProbability(LOCUSNAME_1, ALLELE_1);
        assertEquals(0.15, probabilityStringString, 0.0000001);
        final Double probabilityLocusAllele = stats.getProbability(_locus, _allele1);
        assertEquals(0.15, probabilityLocusAllele, 0.0000001);
    }

    @Test
    public final void testGetAlleles() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        stats.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.15));
        stats.addStatistic(LOCUSNAME_2, ALLELE_1, new BigDecimal(0.01));
        final Collection<String> alleles = stats.getAlleles(LOCUSNAME_1);
        assertNotNull(alleles);
        assertEquals(1, alleles.size());
        assertEquals(ALLELE_1, alleles.iterator().next());
    }

    @Test
    public final void testGetFileName() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        assertEquals(FILE_NAME, stats.getFileName());
    }

    @Test
    public final void testGetLoci() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        final Collection<String> loci = stats.getLoci();
        assertNotNull(loci);
        assertEquals(0, loci.size());

        stats.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.01));

        final Collection<String> loci1 = stats.getLoci();
        assertNotNull(loci1);
        assertEquals(1, loci1.size());

    }

    @Test
    public final void testEqualsObject() {
        final PopulationStatistics stats1 = new PopulationStatistics(FILE_NAME);
        final PopulationStatistics stats2 = new PopulationStatistics(FILE_NAME + "x");
        final PopulationStatistics stats3 = new PopulationStatistics(FILE_NAME);
        final PopulationStatistics stats4 = new PopulationStatistics(FILE_NAME);

        stats1.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.01));
        stats1.addStatistic(LOCUSNAME_2, ALLELE_1, new BigDecimal(0.02));

        stats2.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.01));
        stats2.addStatistic(LOCUSNAME_2, ALLELE_1, new BigDecimal(0.02));

        stats3.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.01));
        stats3.addStatistic(LOCUSNAME_2, ALLELE_1, new BigDecimal(0.02));

        stats4.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.01));

        assertFalse(stats1.equals(stats2));
        assertTrue(stats1.equals(stats3));
        assertFalse(stats1.equals(stats4));
        assertFalse(stats1.equals(FILE_NAME));
        assertFalse(stats1.equals(null));
    }

    @Test
    public final void testFileHash() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        stats.setFileHash("1");
        assertEquals("1", stats.getFileHash());
        stats.setFileHash("2");
        assertEquals("2", stats.getFileHash());
    }

    @Test
    public final void testToString() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        assertEquals(FILE_NAME + " null", stats.toString());
        stats.setFileHash(FILE_HASH);
        assertEquals(FILE_NAME + " " + FILE_HASH, stats.toString());
    }

    @Test
    public final void testIsRareAllele() {
        final PopulationStatistics stats = new PopulationStatistics(FILE_NAME);
        stats.addStatistic(LOCUSNAME_1, ALLELE_1, new BigDecimal(0.15));
        stats.addStatistic(LOCUSNAME_2, ALLELE_1, new BigDecimal(0.01));
        assertTrue(stats.isRareAllele(_rareAllele));
        assertFalse(stats.isRareAllele(_allele1));
    }
}
