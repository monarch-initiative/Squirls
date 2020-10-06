package org.monarchinitiative.squirls.core.data;

import de.charite.compbio.jannovar.data.ReferenceDictionary;

import java.util.Map;
import java.util.Set;

public interface SplicingAnnotationDataSource {

    /**
     * @return reference dictionary being used by this annotation data source
     */
    ReferenceDictionary getReferenceDictionary();

    /**
     * @return set of transcript accessions that are available in the data source
     */
    Set<String> getTranscriptAccessionIds();

    /**
     * @param contig String with contig name, e.g. {@code chrX}
     * @param begin  0-based (excluded) begin coordinate on positive chromosome strand
     * @param end    0-based (included) end coordinate on positive chromosome strand
     * @return map with annotation per gene symbol
     */
    Map<String, SplicingAnnotationData> getAnnotationData(String contig, int begin, int end);

    /**
     * @param contig String with contig name, e.g. {@code chrX}
     * @param pos    1-based coordinate on + chromosome strand
     * @return map with annotation per gene symbol
     */
    default Map<String, SplicingAnnotationData> getAnnotationData(String contig, int pos) {
        return getAnnotationData(contig, pos - 1, pos);
    }

}
