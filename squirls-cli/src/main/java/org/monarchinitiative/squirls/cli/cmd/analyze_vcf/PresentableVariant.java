package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

public class PresentableVariant implements IPresentableVariant {

    private final String variantRepresentation;

    private final String symbol;

    private final double patho;
    private final String content;

    private PresentableVariant(String variantRepresentation, String symbol, double patho, String content) {
        this.variantRepresentation = variantRepresentation;
        this.symbol = symbol;
        this.patho = patho;
        this.content = content;
    }

    public static PresentableVariant of(String variantRepresentation, String symbol, double patho, String content) {
        return new PresentableVariant(variantRepresentation, symbol, patho, content);
    }

    @Override
    public String getVariantRepresentation() {
        return variantRepresentation;
    }

    @Override
    public String getGeneSymbol() {
        return symbol;
    }

    @Override
    public double getMaxPathogenicity() {
        return patho;
    }

    @Override
    public String getContent() {
        return content;
    }
}
