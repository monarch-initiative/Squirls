package org.monarchinitiative.sss.core.reference.fasta;

import org.monarchinitiative.sss.core.model.SequenceInterval;

public interface GenomeSequenceAccessor extends AutoCloseable {

    SequenceInterval fetchSequence(String contig, int begin, int end, boolean strand) throws InvalidCoordinatesException;

}
