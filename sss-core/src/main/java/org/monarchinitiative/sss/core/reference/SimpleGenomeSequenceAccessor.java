package org.monarchinitiative.sss.core.reference;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.FastaSequenceIndexEntry;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * This class allows to fetch arbitrary nucleotide sequence from the reference genome. To do so it requires single
 * Fasta file that contains all contigs. Fasta index (*.fai) is required to be present in the same directory. The
 * index can be created using command <code>samtools faidx file.fa</code> from the <code>samtools</code> suite.
 * <p>
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class SimpleGenomeSequenceAccessor implements GenomeSequenceAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGenomeSequenceAccessor.class);

    private final IndexedFastaSequenceFile fasta;

    private final FastaSequenceIndex index;

    private final File fastaPath;


    /**
     * Create an instance using FASTA file on provided <code>fastaPath</code>.
     *
     * @param fastaPath path to FASTA file. FASTA index is expected to be in the same directory with the same
     *                  basename as the FASTA file + ".fai" suffix
     */
    public SimpleGenomeSequenceAccessor(File fastaPath) {
        this(fastaPath, new File(fastaPath.getAbsolutePath() + ".fai"));
    }


    /**
     * Create an instance using provided FASTA file and index.
     *
     * @param fastaPath path to indexed FASTA file
     * @param indexPath path to FASTA index
     */
    public SimpleGenomeSequenceAccessor(File fastaPath, File indexPath) {
        this.fastaPath = fastaPath;
        LOGGER.debug("Opening indexed fasta file: {}, index: {}", fastaPath.getAbsolutePath(), indexPath.getAbsolutePath());

        index = new FastaSequenceIndex(indexPath);
        fasta = new IndexedFastaSequenceFile(fastaPath, index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        LOGGER.debug("Closing fasta file {}", fastaPath.getAbsolutePath());
        this.fasta.close();
    }

    @Override
    public SequenceInterval fetchSequence(String contig, int begin, int end, boolean strand) throws InvalidCoordinatesException {
        final int contigLength;
        try {
            FastaSequenceIndexEntry entry = index.getIndexEntry(contig);
            contigLength = Math.toIntExact(entry.getSize());

        } catch (SAMException e) {
            throw new InvalidCoordinatesException(String.format("Contig '%s' is not present in FASTA file", contig), e);
        }
        // figure out coordinates on FWD strand
        final int beginOnFwd, endOnFwd;
        if (strand) {
            // we are fetching sequence interval for FWD strand
            beginOnFwd = begin;
            endOnFwd = end;
        } else {
            // we are fetching sequence interval for REV strand
            beginOnFwd = contigLength - end;
            endOnFwd = contigLength - begin;
        }

        final ReferenceSequence referenceSequence;
        try {
            referenceSequence = fasta.getSubsequenceAt(contig, beginOnFwd + 1, endOnFwd);
        } catch (SAMException e) {
            throw new InvalidCoordinatesException(e);
        }

        final String sequence;
        if (strand) {
            sequence = referenceSequence.getBaseString();
        } else {
            // convert fetched sequence to REV strand
            sequence = SequenceInterval.reverseComplement(referenceSequence.getBaseString());
        }

        return SequenceInterval.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig(contig)
                        .setBegin(begin)
                        .setEnd(end)
                        .setStrand(strand)
                        .build())
                .setSequence(sequence)
                .build();
    }

}

