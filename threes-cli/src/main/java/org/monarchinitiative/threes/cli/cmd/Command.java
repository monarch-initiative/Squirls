package org.monarchinitiative.threes.cli.cmd;

import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Command {

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
            LOGGER.info("Initializing progress reporting");
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
