package org.monarchinitiative.squirls.cli.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProgressReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressReporter.class);

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

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
            Instant end = Instant.now();
            Instant begin = localBegin.getAndSet(end);
            Duration duration = Duration.between(begin, end);
            long ms = duration.toMillis();
            LOGGER.info("Processed {} items at {} items/s", current, String.format("%.2f", ((double) tick * 1000) / ms));
        }
    }

    public Runnable summarize() {
        return () -> {
            Duration duration = Duration.between(begin, Instant.now());
            long totalMillis = duration.toMillis();
            double items = count.get();
            double itemsPerSecond = (items * 1000) / totalMillis;
            long mins = (totalMillis / 1000) / 60 % 60;
            long seconds = totalMillis / 1000 % 60;
            LOGGER.info("Processed {} items in {}m {}s ({} totalMillis) at {} items/s",
                    count.get(), mins, seconds, totalMillis, NUMBER_FORMAT.format(itemsPerSecond));
        };
    }
}
