package nl.minvenj.nfi.smartrank.io.searchcriteria.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaReader;

public class JsonReaderFactoryTest {
    private static final String JSON_FILENAME = "SmartRankImportFile-sample.json";

    @Test
    public final void testAccepts() {
        assertTrue(new JsonReaderFactory().accepts("{ \"test\": \"Whatever\" }"));
    }

    @Test
    public final void testDoesNotAccept() {
        assertFalse(new JsonReaderFactory().accepts("<dummy>whatever</dummy>"));
    }

    @Test
    public final void testNewInstance() throws Exception {
        final String json = FileUtils.readFileToString(new File(getClass().getResource(JSON_FILENAME).toURI()));
        final SearchCriteriaReader reader = new JsonReaderFactory().newInstance("JsonReaderFactoryTest", json);
        assertNotNull(reader);
        assertTrue(reader instanceof JsonReader);
    }

}
