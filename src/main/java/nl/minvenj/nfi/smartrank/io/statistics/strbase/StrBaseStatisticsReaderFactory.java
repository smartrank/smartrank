/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io.statistics.strbase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import nl.minvenj.nfi.smartrank.io.statistics.StatisticsFileReader;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsReaderFactory;

public class StrBaseStatisticsReaderFactory implements StatisticsReaderFactory {
    @Override
    public boolean accepts(final File file) {
        return file.getName().toLowerCase().endsWith(".xml");
    }

    @Override
    public StatisticsFileReader create(final File file) throws FileNotFoundException, MalformedURLException, IOException {
        return new StrBaseStatisticsReader(file);
    }
}
