package org.monarchinitiative.threes.core.scoring;

import de.charite.compbio.jannovar.reference.GenomeVariant;

import java.util.Map;

public interface VariantSplicingEvaluator {

    /**
     * Calculate splicing scores for given variant with respect to all transcripts the variant overlaps with.
     *
     * @param variant variant to calculate splicing scores for
     * @return map with splicing pathogenicity data mapped to transcript accession id
     */
    Map<String, SplicingPathogenicityData> evaluate(GenomeVariant variant);

    /**
     * Calculate splicing scores for given variant with respect to all transcripts the variant overlaps with.
     *
     * @param contig string with name of the chromosome
     * @param pos    1-based (included) variant position on FWD strand
     * @param ref    reference allele, e.g. `C`, `CCT`
     * @param alt    alternate allele, e.g. `T`, `AA`
     * @return map with splicing pathogenicity data mapped to transcript accession id
     */
    Map<String, SplicingPathogenicityData> evaluate(String contig, int pos, String ref, String alt);
}
