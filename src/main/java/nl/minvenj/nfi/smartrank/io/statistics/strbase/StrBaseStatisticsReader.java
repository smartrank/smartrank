/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io.statistics.strbase;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXB;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.HashingReader;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsFileReader;
import nl.minvenj.nfi.smartrank.io.strbase.jaxb.FrequenciesDataObject;
import nl.minvenj.nfi.smartrank.io.strbase.jaxb.FrequencyDataObject;
import nl.minvenj.nfi.smartrank.io.strbase.jaxb.MarkerDataObject;
import nl.minvenj.nfi.smartrank.io.strbase.jaxb.OriginDataObject;

public class StrBaseStatisticsReader implements StatisticsFileReader {
    private final File _file;
    private final PopulationStatistics _stats;
    private boolean _initialized;

    public StrBaseStatisticsReader(final File file) {
        _file = file;
        _stats = new PopulationStatistics(file.toString());
    }

    @Override
    public PopulationStatistics getStatistics() throws FileNotFoundException {
        init();
        return _stats;
    }

    private void init() throws FileNotFoundException {
        if (!_initialized) {
            _initialized = true;
            final HashingReader reader = new HashingReader(new java.io.FileReader(_file));
            final FrequenciesDataObject statisticData = JAXB.unmarshal(reader, FrequenciesDataObject.class);

            for (final MarkerDataObject markerData : statisticData.getMarkers()) {
                final String locusName = markerData.getName();
                final List<OriginDataObject> origins = markerData.getOrigins();
                if (origins.size() != 0) {
                    // Last origin is a combined record of all origins.
                    final OriginDataObject fullData = origins.get(origins.size() - 1);
                    for (final FrequencyDataObject frequencyData : fullData.getFrequencies()) {
                        _stats.addStatistic(locusName, Double.toString(frequencyData.getAllele()), new BigDecimal(frequencyData.getValue()));
                    }
                }
            }

            _stats.setFileHash(reader.getHash());
        }
    }
}
