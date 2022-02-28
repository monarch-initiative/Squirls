package org.monarchinitiative.squirls.io.transcript;

import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.squirls.io.transcript.jannovar.IntervalEndExtractor;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.Strand;

class TranscriptEndExtractor implements IntervalEndExtractor<Transcript> {

    private static final TranscriptEndExtractor INSTANCE = new TranscriptEndExtractor();

    static TranscriptEndExtractor instance() {
        return INSTANCE;
    }

    private TranscriptEndExtractor() {
    }

    @Override
    public int getBegin(Transcript tx) {
        return tx.startOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased());
    }

    @Override
    public int getEnd(Transcript tx) {
        return tx.endOnStrandWithCoordinateSystem(Strand.POSITIVE, CoordinateSystem.zeroBased());
    }
}