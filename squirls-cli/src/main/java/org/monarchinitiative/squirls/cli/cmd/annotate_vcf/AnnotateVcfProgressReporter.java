package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

import org.monarchinitiative.squirls.cli.cmd.ProgressReporter;
import org.monarchinitiative.squirls.cli.writers.AnalysisStats;

import java.util.concurrent.atomic.AtomicInteger;

class AnnotateVcfProgressReporter extends ProgressReporter {

    private final AtomicInteger variantCount = new AtomicInteger();

    private final AtomicInteger annotatedAllele = new AtomicInteger();

    /**
     * We report each n-th instance
     */
    AnnotateVcfProgressReporter(int tick) {
        super(tick);
    }

    public <T> void logVariant(T item) {
        variantCount.incrementAndGet();
    }

    public <T> void logAnnotatedAllele(T item) {
        annotatedAllele.incrementAndGet();
    }

    public AnalysisStats getAnalysisStats() {
        return new AnalysisStats(variantCount.get(), alleleCount.get(), annotatedAllele.get());
    }
}
