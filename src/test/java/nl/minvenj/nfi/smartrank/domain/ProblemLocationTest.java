package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ProblemLocationTest {

    private static final String SPECIMEN_NAME = "SpecimenName";
    private static final String LOCUS_NAME = "LocusName";
    private static final String DESCRIPTION = "Message";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testProblemLocation() {
        final ProblemLocation problemLocation = new ProblemLocation(SPECIMEN_NAME, LOCUS_NAME, DESCRIPTION);
        assertEquals(SPECIMEN_NAME, problemLocation.getSpecimen());
        assertEquals(LOCUS_NAME, problemLocation.getLocus());
        assertEquals(DESCRIPTION, problemLocation.getDescription());
    }

    @Test
    public final void testProblemLocationSpecimenNull() {
        final ProblemLocation problemLocation = new ProblemLocation(null, LOCUS_NAME, DESCRIPTION);
        assertEquals("-", problemLocation.getSpecimen());
        assertEquals(LOCUS_NAME, problemLocation.getLocus());
        assertEquals(DESCRIPTION, problemLocation.getDescription());
    }

    @Test
    public final void testProblemLocationLocusNull() {
        final ProblemLocation problemLocation = new ProblemLocation(SPECIMEN_NAME, null, DESCRIPTION);
        assertEquals(SPECIMEN_NAME, problemLocation.getSpecimen());
        assertEquals("-", problemLocation.getLocus());
        assertEquals(DESCRIPTION, problemLocation.getDescription());
    }

    @Test
    public final void testProblemLocationDescriptionNull() {
        final ProblemLocation problemLocation = new ProblemLocation(SPECIMEN_NAME, LOCUS_NAME, null);
        assertEquals(SPECIMEN_NAME, problemLocation.getSpecimen());
        assertEquals(LOCUS_NAME, problemLocation.getLocus());
        assertEquals("", problemLocation.getDescription());
    }
}
