package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

import org.monarchinitiative.squirls.cli.cmd.ProgressReporter;
import org.monarchinitiative.squirls.cli.writers.AnalysisStats;

import java.util.concurrent.atomic.AtomicInteger;

class AnnotateVcfProgressReporter extends ProgressReporter {

    /**
     * We report each n-th instance
     */
    private final AtomicInteger allVariantCount = new AtomicInteger();
    private final AtomicInteger altAlleleCount = new AtomicInteger();
    private final AtomicInteger annotatedAltAlleleCount = new AtomicInteger();
    private final AtomicInteger pathogenicAltAlleleCount = new AtomicInteger();

    AnnotateVcfProgressReporter(int tick) {
        super(tick);
    }

    public <T> void logAltAllele(T item) {
        altAlleleCount.incrementAndGet();
    }

    public <T> void logAnnotatedAllele(T variantDataBox) {
        annotatedAltAlleleCount.incrementAndGet();
    }

    public <T> void logEligibleAllele(T variantDataBox) {
        pathogenicAltAlleleCount.incrementAndGet();
    }

    public AnalysisStats getAnalysisStats() {
        return AnalysisStats.builder()
                .allVariants(allVariantCount.get())
                .alleleCount(altAlleleCount.get())
                .annotatedAlleleCount(annotatedAltAlleleCount.get())
                .pathogenicAlleleCount(pathogenicAltAlleleCount.get())
                .build();
    }
}
