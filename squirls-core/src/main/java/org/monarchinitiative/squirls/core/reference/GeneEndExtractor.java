package org.monarchinitiative.squirls.core.reference;

import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.squirls.core.reference.jannovar.IntervalEndExtractor;
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
    public int getBegin(Gene tx) {
        return tx.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased());
    }

    @Override
    public int getEnd(Gene tx) {
        return tx.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased());
    }
}