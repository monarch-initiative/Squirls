package org.monarchinitiative.squirls.cli.visualization;

/**
 * Members of this enum define what type of visualization we make for splice variants.
 */
public enum VisualizationContext {

    /**
     * For canonical donor variant we show:
     * <ul>
     *     <li>sequence ruler</li>
     *     <li>sequence trekker comparing REF/ALT alleles for the canonical site</li>
     *     <li>position of <em>R<sub>i</sub></em> value of this particular site within the distribution of all possible
     *     changes</li>
     * </ul>
     */
    CANONICAL_DONOR("Canonical donor"),

    /**
     * For canonical acceptor variant we show:
     * <ul>
     *     <li>sequence ruler</li>
     *     <li>sequence trekker comparing REF/ALT alleles for the canonical site</li>
     *     <li>position of <em>R<sub>i</sub></em> value of this particular site within the distribution of all possible
     *     changes</li>
     * </ul>
     */
    CANONICAL_ACCEPTOR("Canonical acceptor"),

    /**
     * For cryptic donor variant we show:
     * <ul>
     *     <li>sequence trekker comparing REF/ALT alleles of the best window of the cryptic site</li>
     *     <li>sequence walker comparision of the best window of the cryptic site with ALT allele of the canonical site</li>
     * </ul>
     */
    CRYPTIC_DONOR("Cryptic donor"),

    /**
     * For cryptic donor variant we show:
     * <ul>
     *     <li>sequence trekker comparing REF/ALT alleles of the best window of the cryptic site</li>
     *     <li>sequence walker comparision of the best window of the cryptic site with ALT allele of the canonical site</li>
     * </ul>
     */
    CRYPTIC_ACCEPTOR("Cryptic acceptor"),

    /**
     * For variant that affects Splicing Regulatory Element (SRE) we show:
     * <ul>
     *     <li>bar plots comparing ESRSeq (hexamer) scores for REF/ALT alleles</li>
     *     <li>bar plots comparing SMS (heptamer) scores for REF/ALT alleles</li>
     * </ul>
     */
    SRE("Splicing regulatory element"),

    /**
     * For variant with undefined visualization rules.
     */
    UNKNOWN("Unknown");

    private final String title;

    VisualizationContext(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
