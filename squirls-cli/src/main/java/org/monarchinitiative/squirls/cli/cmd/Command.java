package org.monarchinitiative.squirls.cli.cmd;

import net.sourceforge.argparse4j.inf.Namespace;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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

    public static class ProgressReporter<T> {

        private static final Logger LOGGER = LoggerFactory.getLogger(ProgressReporter.class);

        /**
         * We report each n-th instance
         */
        private static final int NOTCH = 5_000;

        private final Instant begin;

        private final AtomicInteger count = new AtomicInteger(0);

        public ProgressReporter() {
            begin = Instant.now();
            LOGGER.info("Starting the analysis");
        }

        public void logEntry(T entry) {
            int current = count.incrementAndGet();
            if (current % NOTCH == 0) {
                LOGGER.info("Processed {} items", current);
            }
        }

        public Runnable summarize() {
            return () -> {
                Duration duration = Duration.between(begin, Instant.now());
                long ms = duration.toMillis();
                LOGGER.info("Processed {} items in {}m {}s ({} ms)", count.get(), (ms / 1000) / 60 % 60, ms / 1000 % 60, ms);
            };
        }
    }

}
