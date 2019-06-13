package org.monarchinitiative.threes.core.reference.fasta;

import org.monarchinitiative.threes.core.model.SequenceInterval;

public interface GenomeSequenceAccessor extends AutoCloseable {

    SequenceInterval fetchSequence(String contig, int begin, int end, boolean strand) throws InvalidCoordinatesException;

}
