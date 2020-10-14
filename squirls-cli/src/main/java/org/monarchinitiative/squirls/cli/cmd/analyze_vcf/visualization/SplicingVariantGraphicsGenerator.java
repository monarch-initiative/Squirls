package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization;

import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;

/**
 * The implementing classes generate an appropriate SVG graphics for any given {@link SplicingVariantAlleleEvaluation}.
 * The graphics is then returned as a string.
 */
public interface SplicingVariantGraphicsGenerator {

    /**
     * Generate SVG image for given {@link SplicingVariantAlleleEvaluation}.
     *
     * @param variant {@link SplicingVariantAlleleEvaluation} to be visualized
     * @return SVG graphics
     */
    String generateGraphics(SplicingVariantAlleleEvaluation variant);
}
