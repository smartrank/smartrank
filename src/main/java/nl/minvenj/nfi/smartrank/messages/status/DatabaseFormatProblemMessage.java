package nl.minvenj.nfi.smartrank.messages.status;

import java.util.ArrayList;
import java.util.List;

import nl.minvenj.nfi.smartrank.domain.ProblemLocation;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class DatabaseFormatProblemMessage extends RavenMessage<List<ProblemLocation>> {
    public DatabaseFormatProblemMessage(final ProblemLocation payload) {
        this(buildPayloadList(payload));
    }

    private static List<ProblemLocation> buildPayloadList(final ProblemLocation payload) {
        final ArrayList<ProblemLocation> payloadList = new ArrayList<>();
        payloadList.add(payload);
        return payloadList;
    }

    public DatabaseFormatProblemMessage(final List<ProblemLocation> payload) {
        super(payload, true);
    }
}
