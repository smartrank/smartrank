package nl.minvenj.nfi.smartrank.messages.commands;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class StartAnalysisCommand extends RavenMessage<String> {

    public StartAnalysisCommand() {
        super("");
    }

    @Override
    public boolean isWaitForIdleBus() {
        return true;
    }
}
