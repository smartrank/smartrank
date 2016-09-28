/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io.statistics.strbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;

public class StrBaseStatisticsReaderTest {

    private static final String FREQUENCIES_FILE_HASH = "SHA-1/E0D7077A8A845EC34B8490CA37E4A3BB77F35396";
    private static final String FREQUENCIES_FILE = "frequencies_STRBASE.xml";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testStrBaseStatisticsReaderString() throws FileNotFoundException {
        final StrBaseStatisticsReader reader = new StrBaseStatisticsReader(getTestFile(FREQUENCIES_FILE).getAbsoluteFile());
        final PopulationStatistics statistics = reader.getStatistics();
        assertNotNull(statistics);
        assertEquals(FREQUENCIES_FILE_HASH, statistics.getFileHash());
    }

    private File getTestFile(final String fileName) {
        final URL url = getClass().getResource(fileName);
        return new File(url.getPath());
    }
}
