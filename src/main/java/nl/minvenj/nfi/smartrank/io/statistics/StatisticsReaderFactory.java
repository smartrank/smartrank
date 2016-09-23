/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public interface StatisticsReaderFactory {
    public boolean accepts(File file);

    public StatisticsFileReader create(File file) throws FileNotFoundException, MalformedURLException, IOException;
}
