package org.monarchinitiative.squirls.ingest.transcripts;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import org.monarchinitiative.squirls.core.model.SplicingTranscript;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * POJO for gene data that is inserted into the database within a single transaction.
 */
public class GeneAnnotationData {

    /**
     * Gene HGVS symbol.
     */
    private final String symbol;
    /**
     * Collection of transcripts that belong to a gene.
     */
    private final Collection<SplicingTranscript> transcripts;
    /**
     * Interval that is spanned by the available tracks, such as {@link #refSequence}, or {@link #phylopScores}.
     */
    private final GenomeInterval trackInterval;
    /**
     * Reference sequence of a gene
     */
    private final String refSequence;
    /**
     * phylop conservation scores of a gene.
     */
    private final float[] phylopScores;

    public GeneAnnotationData(String symbol, Collection<SplicingTranscript> transcripts,
                              GenomeInterval trackInterval,
                              String refSequence,
                              float[] phylopScores) {
        this.symbol = symbol;
        this.transcripts = transcripts;
        this.trackInterval = trackInterval;
        this.refSequence = refSequence;
        this.phylopScores = phylopScores;
    }

    public Collection<SplicingTranscript> getTranscripts() {
        return transcripts;
    }

    public GenomeInterval getTrackInterval() {
        return trackInterval;
    }

    public String getRefSequence() {
        return refSequence;
    }

    public float[] getPhylopScores() {
        return phylopScores;
    }


    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneAnnotationData that = (GeneAnnotationData) o;
        return Objects.equals(symbol, that.symbol) &&
                Objects.equals(transcripts, that.transcripts) &&
                Objects.equals(trackInterval, that.trackInterval) &&
                Objects.equals(refSequence, that.refSequence) &&
                Arrays.equals(phylopScores, that.phylopScores);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(symbol, transcripts, trackInterval, refSequence);
        result = 31 * result + Arrays.hashCode(phylopScores);
        return result;
    }

    @Override
    public String toString() {
        return "GeneAnnotationData{" +
                "symbol='" + symbol + '\'' +
                ", transcripts=" + transcripts +
                ", trackInterval=" + trackInterval +
                ", refSequence='" + refSequence + '\'' +
                ", phylopScores=" + Arrays.toString(phylopScores) +
                '}';
    }
}
