package org.monarchinitiative.sss.core;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * This class allows to fetch arbitrary nucleotide sequence from the reference genome. To do so it requires single
 * Fasta file that contains all contigs. Fasta index (*.fai) is required to be present in the same directory. The
 * index can be created using command <code>samtools faidx file.fa</code> from the <code>samtools</code> suite.
 * <p>
 * Chromosome names from UCSC are prefixed <code>'chr'</code>, while chromosomes from ENSEMBL are not. This class is able
 * to fetch sequence from the ENSEMBL build.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class GenomeSequenceAccessor implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenomeSequenceAccessor.class);

    private final IndexedFastaSequenceFile fasta;

    private final File fastaPath;


    /**
     * Create an instance using FASTA file on provided <code>fastaPath</code>.
     *
     * @param fastaPath path to FASTA file. FASTA index is expected to be in the same directory with the same
     *                  basename as the FASTA file + ".fai" suffix
     */
    public GenomeSequenceAccessor(File fastaPath) {
        this(fastaPath, new File(fastaPath.getAbsolutePath() + ".fai"));
    }


    /**
     * Create an instance using provided FASTA file and index.
     *
     * @param fastaPath path to indexed FASTA file
     * @param indexPath path to FASTA index
     */
    public GenomeSequenceAccessor(File fastaPath, File indexPath) {
        this.fastaPath = fastaPath;
        LOGGER.debug("Opening indexed fasta file: {}, index: {}", fastaPath.getAbsolutePath(), indexPath.getAbsolutePath());
        FastaSequenceIndex fastaIndex = new FastaSequenceIndex(indexPath);
        fasta = new IndexedFastaSequenceFile(fastaPath, fastaIndex);
    }


    /**
     * Get sequence of nucleotides from given position specified by chromosome/contig name, starting position and ending
     * position. Case of nucleotides is not changed.
     * <p>
     * Querying with negative coordinates does not raise an exception, querying with e.g.
     * <code>fetchSequence("chr8", -6, -1)</code> returns ">chr8". However it does have any sense to do it.
     *
     * @param chr   chromosome name, prefix 'chr' will be removed if present
     * @param start start position using 0-based numbering (exclusive)
     * @param end   end chromosomal position using 0-based numbering (inclusive)
     * @return nucleotide sequence or <code>null</code> if coordinates ask for a region beyond the end of the chromosome
     * or if the chromosome is not present in the FASTA file
     */
    public String fetchSequence(String chr, int start, int end) {
        // Chromosomes from ENSEMBL do not have prefix 'chr'
        String chrom = (chr.startsWith("chr")) ? chr.substring(3) : chr;
        ReferenceSequence referenceSequence;
        try {
            referenceSequence = fasta.getSubsequenceAt(chrom, start + 1, end);
        } catch (SAMException e) { // start or end position is beyond the end of contig, chromosome is not present in the FASTA file
            return null;
        }
        return new String(referenceSequence.getBases());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        LOGGER.debug("Closing fasta file {}", fastaPath.getAbsolutePath());
        this.fasta.close();
    }
}
