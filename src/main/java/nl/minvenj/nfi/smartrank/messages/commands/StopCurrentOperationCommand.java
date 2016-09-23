package nl.minvenj.nfi.smartrank.messages.commands;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class StopCurrentOperationCommand extends RavenMessage<String> {

    public StopCurrentOperationCommand() {
        super("");
    }

}
