package org.monarchinitiative.threes.core.data;


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
     * @param contig
     * @param begin  0-based position on
     * @param end    0-based inclusive end position on FWD strand
     * @return
     */
    List<SplicingTranscript> fetchTranscripts(String contig, int begin, int end);
}
