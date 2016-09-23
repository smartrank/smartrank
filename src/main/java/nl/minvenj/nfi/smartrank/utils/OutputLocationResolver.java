package nl.minvenj.nfi.smartrank.utils;

import java.io.File;

import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;
import nl.minvenj.nfi.smartrank.messages.data.OutputLocationMessage;
import nl.minvenj.nfi.smartrank.raven.NullUtils;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class OutputLocationResolver {

    public static String resolve(final String fileName) {
        final String outputLocation = NullUtils.getValue(MessageBus.getInstance().query(OutputLocationMessage.class), "");
        if (!outputLocation.isEmpty()) {
            File outFolder = new File(outputLocation);

            if (!outFolder.isAbsolute()) {
                outFolder = new File(SmartRankRestrictions.getOutputRootFolder(), outputLocation);
            }

            final File outFile = new File(outFolder, new File(fileName).getName());
            return outFile.getAbsolutePath();
        }
        return fileName;
    }

}
