package org.monarchinitiative.squirls.cli.visualization;

import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.data.SplicingVariantAlleleEvaluation;

/**
 * The implementing classes generate an appropriate SVG graphics for any given {@link SplicingVariantAlleleEvaluation}.
 * The graphics is then returned as a string.
 */
public interface SplicingVariantGraphicsGenerator {

    /**
     * The image returned if unable to generate a normal SVG.
     */
    String EMPTY_SVG_IMAGE = "<svg width=\"100\" height=\"5\" xmlns=\"http://www.w3.org/2000/svg\"></svg>";

    /**
     * Generate SVG image for given {@link SplicingVariantAlleleEvaluation}.
     *
     * @param variant to be visualized
     * @return SVG graphics
     */
    String generateGraphics(VisualizedVariant variant);
}
