/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.statistics.defaultcsv.DefaultStatisticsReaderFactory;
import nl.minvenj.nfi.smartrank.io.statistics.strbase.StrBaseStatisticsReaderFactory;

public class StatisticsReader {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsReader.class);
    private static final ArrayList<StatisticsReaderFactory> STATISTIC_READER_FACTORIES = new ArrayList<>();

    static {
        STATISTIC_READER_FACTORIES.add(new DefaultStatisticsReaderFactory());
        STATISTIC_READER_FACTORIES.add(new StrBaseStatisticsReaderFactory());
    }

    private StatisticsFileReader _reader;

    public StatisticsReader(final File file) throws MalformedURLException, IOException {
        if (file == null) {
            throw new NullPointerException("Input file was null");
        }

        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }

        for (final StatisticsReaderFactory readerFactory : STATISTIC_READER_FACTORIES) {
            if (readerFactory.accepts(file)) {
                _reader = readerFactory.create(file);
                return;
            }
        }

        throw new IllegalArgumentException("Unknown file format for '" + file + "'");
    }

    public PopulationStatistics getStatistics() throws FileNotFoundException {
        return _reader.getStatistics();
    }
}
