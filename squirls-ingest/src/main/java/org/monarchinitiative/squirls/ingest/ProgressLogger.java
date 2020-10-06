package org.monarchinitiative.squirls.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Utility class for progress reporting
 */
public class ProgressLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressLogger.class);

    private final AtomicInteger total = new AtomicInteger();

    private final int reportingEach;

    public ProgressLogger() {
        this(5_000);
    }

    public ProgressLogger(int reportingEach) {
        this.reportingEach = reportingEach;
    }

    public AtomicInteger getTotal() {
        return total;
    }

    public Consumer<Object> logTotal() {
        return logTotal("");
    }

    public Consumer<Object> logTotal(String msg) {
        return t -> {
            int i = total.incrementAndGet();
            if (msg != null && !msg.isEmpty()) {
                if (i % reportingEach == 0) {
                    LOGGER.info(msg, i);
                }
            }
        };
    }

}
