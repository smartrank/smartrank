package nl.minvenj.nfi.smartrank.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.junit.Test;

public class FilenameLengthCheckerTest {

    String[][] _values = {
        {"thisFilenameIsOK.log", "thisFilenameIsOK.log"},
        {"/thispathistoolong/butcanbefixedby/shorteningthefilename.txt", "/thispathistoolong/butcanbefixedby/shorte(12).txt"},
        {"/thispathistoolong/andcannotbefixedby/shorteningthefilename/becausethepathisjusttoolong.txt", "/thispathistoolong/andcannotbefixedby/shorteningthefilename/becausethepathisjusttoolong.txt"},

    };

    /**
     * A filter that checks for the presence of a certain index in the filename. This to mimic file existence/nonexistence.
     */
    private class IndexCheckerFilter implements FileFilter {
        private final int _index;

        public IndexCheckerFilter(final int index) {
            _index = index;
        }

        @Override
        public boolean accept(final File file) {
            return file.getAbsolutePath().contains("(" + _index + ")");
        }
    }

    @Test
    public final void testCheck() throws IOException {
        for (final String[] tuple : _values) {
            final String string = FilenameLengthChecker.check(tuple[0], new IndexCheckerFilter(12), 50);
            assertEquals(tuple[1], string);
        }
    }
}
