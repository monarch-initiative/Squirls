package org.monarchinitiative.squirls.cli.cmd;

import net.sourceforge.argparse4j.inf.Namespace;
import org.monarchinitiative.squirls.core.SplicingPredictionData;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class Command {

    /**
     * Process predictions for transcripts into a single record in format <code>NM_123456.7=0.88;ENST00000123456.5=0.99</code>
     *
     * @param predictionData map with predictions
     * @return record
     */
    protected static String processScores(Map<String, SplicingPredictionData> predictionData) {
        return predictionData.keySet().stream()
                .sorted()
                .map(tx -> String.format("%s=%f", tx, predictionData.get(tx).getPrediction().getMaxPathogenicity()))
                .collect(Collectors.joining(";"));
    }

    public abstract void run(Namespace namespace) throws CommandException;

}
