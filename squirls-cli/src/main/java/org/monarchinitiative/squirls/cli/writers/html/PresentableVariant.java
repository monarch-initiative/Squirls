package org.monarchinitiative.squirls.cli.writers.html;


import java.util.Objects;

/**
 * This class represents variant data that is available within the variant box in the HTML report.
 */
public class PresentableVariant {

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

    public String getVariantRepresentation() {
        return variantRepresentation;
    }


    public String getGeneSymbol() {
        return symbol;
    }


    public double getMaxPathogenicity() {
        return patho;
    }


    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PresentableVariant that = (PresentableVariant) o;
        return Double.compare(that.patho, patho) == 0 &&
                Objects.equals(variantRepresentation, that.variantRepresentation) &&
                Objects.equals(symbol, that.symbol) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantRepresentation, symbol, patho, content);
    }

    @Override
    public String toString() {
        return "PresentableVariant{" +
                "variantRepresentation='" + variantRepresentation + '\'' +
                ", symbol='" + symbol + '\'' +
                ", patho=" + patho +
                ", content='" + content + '\'' +
                '}';
    }
}
