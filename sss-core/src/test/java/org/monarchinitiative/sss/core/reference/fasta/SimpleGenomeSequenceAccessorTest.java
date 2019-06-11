package org.monarchinitiative.sss.core.reference.fasta;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.sss.core.model.GenomeCoordinates;
import org.monarchinitiative.sss.core.model.SequenceInterval;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class SimpleGenomeSequenceAccessorTest {

    private static File FASTA_PATH;

    private static File FASTA_IDX_PATH;

    private SimpleGenomeSequenceAccessor instance;


    @BeforeAll
    static void setUpBefore() {
        /*
        This FASTA file contains first 49950 nucleotides of all canonical chromosomes. Chromosomes are labeled '1, 2, ..., 22, X, Y, M'.
         */
        FASTA_PATH = new File(SimpleGenomeSequenceAccessorTest.class.getResource("shortHg19.fa").getFile());
        FASTA_IDX_PATH = new File(SimpleGenomeSequenceAccessorTest.class.getResource("shortHg19.fa.fai").getFile());
    }


    @BeforeEach
    void setUp() {
        instance = new SimpleGenomeSequenceAccessor(FASTA_PATH, FASTA_IDX_PATH);
    }


    @AfterEach
    void tearDown() throws Exception {
        instance.close();
    }


    @Test
    void testFetchSequenceFromBeyondEndOfContig() throws Exception {
        // This query asks for one nucleotide past end of chromosome 8
        assertThrows(InvalidCoordinatesException.class, () -> instance.fetchSequence("8", 49950, 49951, true));

        // one nucleotide of query interval is located past end of chromosome 8
        assertThrows(InvalidCoordinatesException.class, () -> instance.fetchSequence("8", 49900, 49951, true));
    }


    @Test
    void testSingleFileConstructor() throws Exception {
        GenomeSequenceAccessor accessor = new SimpleGenomeSequenceAccessor(FASTA_PATH);
        assertThat(accessor.fetchSequence("7", 44580, 44620, true),
                is(SequenceInterval.newBuilder()
                        .setCoordinates(GenomeCoordinates.newBuilder()
                                .setContig("7")
                                .setBegin(44580)
                                .setEnd(44620)
                                .setStrand(true)
                                .build())
                        .setSequence("AGAGGAGGAAACGTGAATAGTATGCAGCTTCCCGCACACA")
                        .build()
                ));
    }


    @Test
    void fetchSequenceOnFwdStrand() throws Exception {
        SequenceInterval si = instance.fetchSequence("1", 10000, 10100, true);
        assertThat(si, is(SequenceInterval.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("1")
                        .setBegin(10000)
                        .setEnd(10100)
                        .setStrand(true)
                        .build())
                .setSequence("taaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaac")
                .build()));
    }

    @Test
    void fetchSequenceOnRevStrand() throws Exception {
        SequenceInterval si = instance.fetchSequence("2", 10000, 10100, false);
        assertThat(si, is(SequenceInterval.newBuilder()
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("2")
                        .setBegin(10000)
                        .setEnd(10100)
                        .setStrand(false)
                        .build())
                .setSequence("TGGTTGCTCTAAAAATGCTGCTATTTTGCTGTTCACTGTATTGCACTTAGTTAAAAAGAAGATAATGTGAAAGATGAGAGCAGTTTTTTAAAGGATCTTT")
                .build()));
    }

    @Test
    void nonExistingContigThrowsException() {
        assertThrows(InvalidCoordinatesException.class, () -> instance.fetchSequence("BAD_CONTIG", 10000, 10100, true));
    }


}