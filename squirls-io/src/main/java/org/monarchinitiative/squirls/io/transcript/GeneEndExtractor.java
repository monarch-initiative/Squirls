package org.monarchinitiative.squirls.io.transcript;

import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.squirls.io.transcript.jannovar.IntervalEndExtractor;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Strand;

class GeneEndExtractor implements IntervalEndExtractor<Gene> {

    private static final GeneEndExtractor INSTANCE = new GeneEndExtractor();

    static GeneEndExtractor instance() {
        return INSTANCE;
    }

    private GeneEndExtractor() {
    }

    @Override
    public int getBegin(Gene gene) {
        return gene.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased());
    }

    @Override
    public int getEnd(Gene gene) {
        return gene.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased());
    }
}