package org.monarchinitiative.sss.core.reference;

import org.monarchinitiative.sss.core.model.SequenceInterval;

import java.io.File;

/**
 * Contigs in UCSC reference FASTA file are prefixed <code>'chr'</code>, while contigs in ENSEMBL are not.
 * This {@link GenomeSequenceAccessor} is able to fetch sequence from FASTA files from both reference sources.
 */
public class PrefixHandlingGenomeSequenceAccessor implements GenomeSequenceAccessor {

    public PrefixHandlingGenomeSequenceAccessor(File fasta, File fastaIdx) {

    }

    @Override
    public SequenceInterval fetchSequence(String contig, int begin, int end, boolean strand) throws InvalidCoordinatesException {
        // TODO - implement
        // /**
        //     * Get sequence of nucleotides from given position specified by chromosome/contig name, starting position and ending
        //     * position. Case of nucleotides is not changed.
        //     * <p>
        //     * Querying with negative coordinates does not raise an exception, querying with e.g.
        //     * <code>fetchSequence("chr8", -6, -1)</code> returns ">chr8". However it does have any sense to do it.
        //     *
        //     * @param chr   chromosome name, prefix 'chr' will be removed if present
        //     * @param start start position using 0-based numbering (exclusive)
        //     * @param end   end chromosomal position using 0-based numbering (inclusive)
        //     * @return nucleotide sequence or <code>null</code> if coordinates ask for a region beyond the end of the chromosome
        //     * or if the chromosome is not present in the FASTA file
        //     */
        //    @Deprecated
        //    public String fetchSequence(String chr, int start, int end) {
        //        // Chromosomes from ENSEMBL do not have prefix 'chr'
        //        String chrom = (chr.startsWith("chr")) ? chr.substring(3) : chr;
        //        ReferenceSequence referenceSequence;
        //        try {
        //            referenceSequence = fasta.getSubsequenceAt(chrom, start + 1, end);
        //        } catch (SAMException e) { // start or end position is beyond the end of contig, chromosome is not present in the FASTA file
        //            return null;
        //        }
        //        return new String(referenceSequence.getBases());
        //    }
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
