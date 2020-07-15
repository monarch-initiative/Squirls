package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.simple;

/**
 * Members of this enum define which figures to show for a given variant.
 */
enum VisualizationContext {

    /**
     * For canonical donor variant we show:
     * <ul>
     *     <li>sequence ruler</li>
     *     <li>sequence trekker comparing REF/ALT alleles for the canonical site</li>
     *     <li>position of <em>R<sub>i</sub></em> value of this particular site within the distribution of all possible
     *     changes</li>
     * </ul>
     */
    CANONICAL_DONOR,

    /**
     * For canonical acceptor variant we show:
     * <ul>
     *     <li>sequence ruler</li>
     *     <li>sequence trekker comparing REF/ALT alleles for the canonical site</li>
     *     <li>position of <em>R<sub>i</sub></em> value of this particular site within the distribution of all possible
     *     changes</li>
     * </ul>
     */
    CANONICAL_ACCEPTOR,

    /**
     * For cryptic donor variant we show:
     * <ul>
     *     <li>sequence trekker comparing REF/ALT alleles of the best window of the cryptic site</li>
     *     <li>sequence walker comparision of the best window of the cryptic site with ALT allele of the canonical site</li>
     * </ul>
     */
    CRYPTIC_DONOR,

    /**
     * For cryptic donor variant we show:
     * <ul>
     *     <li>sequence trekker comparing REF/ALT alleles of the best window of the cryptic site</li>
     *     <li>sequence walker comparision of the best window of the cryptic site with ALT allele of the canonical site</li>
     * </ul>
     */
    CRYPTIC_ACCEPTOR,

    /**
     * For variant that affects Splicing Regulatory Element (SRE) we show:
     * <ul>
     *     <li>bar plots comparing ESRSeq (hexamer) scores for REF/ALT alleles</li>
     *     <li>bar plots comparing SMS (heptamer) scores for REF/ALT alleles</li>
     * </ul>
     */
    SRE,

    /**
     * For variant with undefined visualization rules.
     */
    UNKNOWN


}
