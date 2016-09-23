package nl.minvenj.nfi.smartrank.messages.commands;

import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class ClearSearchCriteriaMessage extends RavenMessage<String> {
    public ClearSearchCriteriaMessage() {
        super("");
    }
}
