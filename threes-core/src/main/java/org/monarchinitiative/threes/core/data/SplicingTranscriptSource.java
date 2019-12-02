package org.monarchinitiative.threes.core.data;


import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.monarchinitiative.threes.core.model.SplicingTranscript;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface SplicingTranscriptSource {

    List<String> getTranscriptAccessionIds();

    /**
     * Fetch transcripts that overlap with query interval specified by <code>contig</code>, <code>begin</code>, and
     * <code>end</code>.
     *
     * @param contig              string with contig id, e.g. `chr1`, `X`
     * @param begin               0-based (exclusive) begin position on FWD strand
     * @param end                 0-based (inclusive) end position on FWD strand
     * @param referenceDictionary reference dictionary to use to create SplicingTranscript data
     * @return list with all transcripts that overlap with query interval
     */
    List<SplicingTranscript> fetchTranscripts(String contig, int begin, int end, ReferenceDictionary referenceDictionary);

    /**
     * Fetch transcript by accession ID.
     *
     * @param txAccession         transcript accession ID, e.g. `NM_004004.2`
     * @param referenceDictionary reference dictionary to use to create SplicingTranscript data
     * @return {@link Optional} with transcript data. The optional is empty if no such transcript is present in the database
     */
    Optional<SplicingTranscript> fetchTranscriptByAccession(String txAccession, ReferenceDictionary referenceDictionary);
}
