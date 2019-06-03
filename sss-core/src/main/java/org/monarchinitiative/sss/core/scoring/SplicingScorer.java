package org.monarchinitiative.sss.core.scoring;

/**
 *
 */
public interface SplicingScorer {

    double score(String contig, int pos, String allele);

}
