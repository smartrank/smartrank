package nl.minvenj.nfi.smartrank.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ProblemLocationTest {

    private static final long LOCATION = 1234;
    private static final String SPECIMEN_NAME = "SpecimenName";
    private static final String LOCUS_NAME = "LocusName";
    private static final String DESCRIPTION = "Message";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testProblemLocation() {
        final ProblemLocation problemLocation = new ProblemLocation(LOCATION, SPECIMEN_NAME, LOCUS_NAME, DESCRIPTION);
        assertEquals(LOCATION, problemLocation.getLocation());
        assertEquals(SPECIMEN_NAME, problemLocation.getSpecimen());
        assertEquals(LOCUS_NAME, problemLocation.getLocus());
        assertEquals(DESCRIPTION, problemLocation.getDescription());
    }

    @Test
    public final void testProblemLocationSpecimenNull() {
        final ProblemLocation problemLocation = new ProblemLocation(LOCATION, null, LOCUS_NAME, DESCRIPTION);
        assertEquals(LOCATION, problemLocation.getLocation());
        assertEquals("-", problemLocation.getSpecimen());
        assertEquals(LOCUS_NAME, problemLocation.getLocus());
        assertEquals(DESCRIPTION, problemLocation.getDescription());
    }

    @Test
    public final void testProblemLocationLocusNull() {
        final ProblemLocation problemLocation = new ProblemLocation(LOCATION, SPECIMEN_NAME, null, DESCRIPTION);
        assertEquals(LOCATION, problemLocation.getLocation());
        assertEquals(SPECIMEN_NAME, problemLocation.getSpecimen());
        assertEquals("-", problemLocation.getLocus());
        assertEquals(DESCRIPTION, problemLocation.getDescription());
    }

    @Test
    public final void testProblemLocationDescriptionNull() {
        final ProblemLocation problemLocation = new ProblemLocation(LOCATION, SPECIMEN_NAME, LOCUS_NAME, null);
        assertEquals(LOCATION, problemLocation.getLocation());
        assertEquals(SPECIMEN_NAME, problemLocation.getSpecimen());
        assertEquals(LOCUS_NAME, problemLocation.getLocus());
        assertEquals("", problemLocation.getDescription());
    }
}
