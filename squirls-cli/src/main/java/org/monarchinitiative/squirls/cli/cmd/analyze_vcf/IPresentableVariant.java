package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

/**
 * This interface represents splice variant that is being visualized in a box in the HTML report.
 */
public interface IPresentableVariant {

    /**
     * @return a string like <code>chr1:1234C>A</code>
     */
    String getVariantRepresentation();

    /**
     * @return string with HGVS gene symbol
     */
    String getGeneSymbol();

    /**
     * @return splicing pathogenicity estimate
     */
    double getMaxPathogenicity();

    /**
     * @return HTML-formatted string with content to present in the variant box
     */
    String getContent();

}
