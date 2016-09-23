package nl.minvenj.nfi.smartrank.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.io.statistics.StatisticsReader;

public class StatsConverter {
    public static void main(final String[] args) throws MalformedURLException, IOException {
        final StatisticsReader statisticsReader = new StatisticsReader(new File(args[0]));
        final PopulationStatistics statistics = statisticsReader.getStatistics();
        final File outFile = new File(args[0] + ".xml");
        try (final FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write("<Statistics>\n".getBytes());
            for (final String locusName : statistics.getLoci()) {
                fos.write(("    <Locus name='" + locusName + "'>\n").getBytes());
                for (final String allele : statistics.getAlleles(locusName)) {
                    final Double probability = statistics.getProbability(locusName, allele);
                    fos.write(("        <Allele value='" + allele + "' probability='" + probability + "' />\n").getBytes());
                }
                fos.write(("    </Locus>\n").getBytes());
            }
            fos.write("</Statistics>\n".getBytes());
        }
    }

}
