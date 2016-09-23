/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io.statistics;

import java.io.FileNotFoundException;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;

public interface StatisticsFileReader {
    public PopulationStatistics getStatistics() throws FileNotFoundException;
}
