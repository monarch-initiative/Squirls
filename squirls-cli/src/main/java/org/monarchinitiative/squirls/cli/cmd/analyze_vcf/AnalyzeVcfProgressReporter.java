package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import org.monarchinitiative.squirls.cli.cmd.ProgressReporter;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.AnalysisStats;

import java.util.concurrent.atomic.AtomicInteger;

class AnalyzeVcfProgressReporter extends ProgressReporter {

    /**
     * We report each n-th instance
     */
    private final AtomicInteger allVariantCount = new AtomicInteger();
    private final AtomicInteger altAlleleCount = new AtomicInteger();
    private final AtomicInteger annotatedAltAlleleCount = new AtomicInteger();
    private final AtomicInteger pathogenicAltAlleleCount = new AtomicInteger();

    AnalyzeVcfProgressReporter(int tick) {
        super(tick);
    }

    public void logAltAllele(Object variantDataBox) {
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
