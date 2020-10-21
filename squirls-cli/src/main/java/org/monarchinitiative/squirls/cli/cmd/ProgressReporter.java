package org.monarchinitiative.squirls.cli.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProgressReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressReporter.class);

    protected final Instant begin;
    /**
     * We report each n-th instance
     */
    private final int tick;
    private final AtomicReference<Instant> localBegin;

    private final AtomicInteger count = new AtomicInteger(0);

    public ProgressReporter(int tick) {
        this.tick = tick;
        begin = Instant.now();
        localBegin = new AtomicReference<>(begin);
        LOGGER.info("Starting the analysis");
    }

    public <T> void logItem(T entry) {
        int current = count.incrementAndGet();
        if (current % tick == 0) {
            final Instant end = Instant.now();
            final Instant begin = localBegin.getAndSet(end);
            final Duration duration = Duration.between(begin, end);
            final long ms = duration.toMillis();
            LOGGER.info("Processed {} items at {} items/s", current, String.format("%.2f", ((double) tick * 1000) / ms));
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
