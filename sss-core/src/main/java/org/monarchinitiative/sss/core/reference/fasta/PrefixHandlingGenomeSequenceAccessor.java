package org.monarchinitiative.sss.core.reference.fasta;

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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Contigs in UCSC reference FASTA file are prefixed <code>'chr'</code>, while contigs in ENSEMBL are not.
 * This {@link GenomeSequenceAccessor} is able to fetch sequence from FASTA files from both reference sources.
 */
public class PrefixHandlingGenomeSequenceAccessor implements GenomeSequenceAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGenomeSequenceAccessor.class);

    private final IndexedFastaSequenceFile fasta;

    private final FastaSequenceIndex index;

    private final File fastaPath;

    /**
     * True if contigs in the reference FASTA file look like chr1, chr2, ..., chrX, chrY
     */
    private final boolean contigsArePrefixed;

    /**
     * True if mitochondrial contig is denoted as chrMT and <em>not</em> as chrM
     */
    private final boolean mitochondrialIsMt;

    public PrefixHandlingGenomeSequenceAccessor(File fasta, File fastaIdx) throws InvalidFastaFileException {
        this.index = new FastaSequenceIndex(fastaIdx);
        this.fasta = new IndexedFastaSequenceFile(fasta, index);
        this.fastaPath = fasta;
        this.contigsArePrefixed = figureOutPrefixes(index);
        this.mitochondrialIsMt = figureOutMitochondrial(index);
    }

    private static boolean figureOutMitochondrial(FastaSequenceIndex index) {
        Predicate<FastaSequenceIndexEntry> isMT = e -> e.getContig().contains("MT");
        return StreamSupport.stream(index.spliterator(), false).anyMatch(isMT);
    }

    private static boolean figureOutPrefixes(FastaSequenceIndex index) throws InvalidFastaFileException {
        List<FastaSequenceIndexEntry> entries = StreamSupport.stream(index.spliterator(), false)
                .collect(Collectors.toList());
        Predicate<FastaSequenceIndexEntry> prefixed = e -> e.getContig().startsWith("chr");
        boolean allPrefixed = entries.stream().allMatch(prefixed);
        boolean nonePrefixed = entries.stream().noneMatch(prefixed);

        if (allPrefixed) {
            return true;
        } else if (nonePrefixed) {
            return false;
        } else {
            String msg = String.format("Found prefixed and unprefixed contigs among fasta index entries - %s", entries.stream()
                    .map(FastaSequenceIndexEntry::getContig).collect(Collectors.joining(",", "{", "}")));
            LOGGER.error(msg);
            throw new InvalidFastaFileException(msg);
        }
    }

    private String doctorContig(String contig) {
        final String doctoredContig;
        if (contigsArePrefixed) {
            if (contig.startsWith("chr")) {
                doctoredContig = contig;
            } else {
                doctoredContig = "chr" + contig;
            }
        } else {
            if (contig.startsWith("chr")) {
                doctoredContig = contig.substring(3);
            } else {
                doctoredContig = contig;
            }
        }

        if (doctoredContig.contains("M")) {
            // this is mitochondrial
            if (mitochondrialIsMt) {
                // reference - chrMT|MT
                if (!doctoredContig.contains("MT")) {
                    // contig - chrM|M
                    return doctoredContig.replace("M", "MT");
                }
                // contig - chrMT|MT, returned at the bottom
            } else {
                // reference - chrM|M
                if (doctoredContig.contains("MT")) {
                    // contig - chrMT|MT
                    return doctoredContig.replace("MT", "M");
                }
                // contig - chrM|M, returned at the bottom
            }
        }
        return doctoredContig;
    }

    @Override
    public SequenceInterval fetchSequence(String contig, int begin, int end, boolean strand) throws InvalidCoordinatesException {
        // add prefix if necessary
        final String doctoredContig = doctorContig(contig);


        final int contigLength;
        try {
            FastaSequenceIndexEntry entry = index.getIndexEntry(doctoredContig);
            contigLength = Math.toIntExact(entry.getSize());
        } catch (SAMException e) {
            throw new InvalidCoordinatesException(String.format("Contig '%s' is not present in FASTA file", doctoredContig), e);
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
            referenceSequence = fasta.getSubsequenceAt(doctoredContig, beginOnFwd + 1, endOnFwd);
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

    @Override
    public void close() throws Exception {
        LOGGER.debug("Closing fasta file {}", fastaPath.getAbsolutePath());
        this.fasta.close();
    }
}
