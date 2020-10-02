package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;

import java.util.Collection;
import java.util.Optional;

public interface SplicingAnnotationDataSource<T extends SplicingAnnotationData> {

    Collection<String> getTranscriptAccessionIds();

    /**
     * @param contig string with contig id, e.g. `chr1`, `X`
     * @param begin  0-based (exclusive) begin position on FWD strand
     * @param end    0-based (inclusive) end position on FWD strand
     * @param rd     reference dictionary to use to create SplicingTranscript data
     * @return collection annotation with respect to all transcripts that overlap with the coordinates
     */
    <T> Collection <T> getAnnotations(String contig, int begin, int end, ReferenceDictionary rd);

    /**
     * Fetch data by transcript accession ID.
     *
     * @param txAccession         transcript accession ID, e.g. `NM_004004.2`
     * @param referenceDictionary reference dictionary to use to create SplicingTranscript data
     * @return {@link Optional} with transcript data. The optional is empty if no such transcript is present in the database
     */
    Optional<T> fetchAnnotationsByTxAccession(String txAccession, ReferenceDictionary referenceDictionary);

}
