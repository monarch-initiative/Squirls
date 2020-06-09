package org.monarchinitiative.squirls.core;

import java.util.Map;
import java.util.Set;

public interface VariantSplicingEvaluator {

    /**
     * Calculate splicing scores for given variant with respect to given transcript IDs (<code>txIds</code>).
     * The variant is evaluated with respect to all overlapping transcripts, if <code>txIds</code> is empty.
     *
     * @param contig string with name of the chromosome
     * @param pos    1-based (included) variant position on FWD strand
     * @param ref    reference allele, e.g. `C`, `CCT`
     * @param alt    alternate allele, e.g. `T`, `AA`
     * @param txIds  set of transcript accession IDs with respect to which the variant should be evaluated
     * @return splicing prediction data
     */
    Map<String, SplicingPredictionData> evaluate(String contig, int pos, String ref, String alt, Set<String> txIds);

    /**
     * Calculate splicing scores for given variant with respect to all transcripts the variant overlaps with.
     *
     * @param contig string with name of the chromosome
     * @param pos    1-based (included) variant position on FWD strand
     * @param ref    reference allele, e.g. `C`, `CCT`
     * @param alt    alternate allele, e.g. `T`, `AA`
     * @return map with splicing pathogenicity data mapped to transcript accession id
     */
    default Map<String, SplicingPredictionData> evaluate(String contig, int pos, String ref, String alt) {
        return evaluate(contig, pos, ref, alt, Set.of());
    }
}
