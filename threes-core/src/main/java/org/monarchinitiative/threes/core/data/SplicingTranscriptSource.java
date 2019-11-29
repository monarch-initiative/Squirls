package org.monarchinitiative.threes.core.data;


import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.monarchinitiative.threes.core.model.SplicingTranscript;

import java.util.List;

/**
 *
 */
public interface SplicingTranscriptSource {


    /**
     * Fetch transcripts that overlap with query interval specified by <code>contig</code>, <code>begin</code>, and
     * <code>end</code>.
     *
     * @param contig string with contig id, e.g. `chr1`, `X`
     * @param begin  0-based (exclusive) begin position on FWD strand
     * @param end    0-based (inclusive) end position on FWD strand
     * @return
     */
    List<SplicingTranscript> fetchTranscripts(String contig, int begin, int end, ReferenceDictionary referenceDictionary);

    // TODO - implement
//    Optional<SplicingTranscript> fetchTranscript(String contig, int begin, int end, String txAccession);
}
