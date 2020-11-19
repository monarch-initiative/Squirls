package org.monarchinitiative.squirls.cli.visualization;


/**
 * The implementing classes generate an appropriate SVG graphics for any given {@link VisualizableVariantAllele}.
 * The graphics is then returned as a string.
 */
public interface SplicingVariantGraphicsGenerator {

    /**
     * The image returned if unable to generate a normal SVG.
     */
    String EMPTY_SVG_IMAGE = "<svg width=\"100\" height=\"5\" xmlns=\"http://www.w3.org/2000/svg\"></svg>";

    /**
     * Generate SVG image for given {@link VisualizableVariantAllele}.
     *
     * @param variant to be visualized
     * @return SVG graphics
     */
    String generateGraphics(VisualizableVariantAllele variant);
}
