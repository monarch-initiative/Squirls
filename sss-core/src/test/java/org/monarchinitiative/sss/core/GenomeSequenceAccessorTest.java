package org.monarchinitiative.sss.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class GenomeSequenceAccessorTest {

    private static File FASTA_PATH;

    private static File FASTA_IDX_PATH;

    private GenomeSequenceAccessor instance;


    @BeforeAll
    static void setUpBefore() {
        /*
        This FASTA file contains first 49950 nucleotides of all canonical chromosomes. Chromosomes are labeled '1, 2, ..., 22, X, Y, M'.
         */
        FASTA_PATH = new File(GenomeSequenceAccessorTest.class.getResource("shortHg19.fa").getFile());
        FASTA_IDX_PATH = new File(GenomeSequenceAccessorTest.class.getResource("shortHg19.fa.fai").getFile());
    }


    @BeforeEach
    void setUp() {
        instance = new GenomeSequenceAccessor(FASTA_PATH, FASTA_IDX_PATH);
    }


    @AfterEach
    void tearDown() throws Exception {
        instance.close();
    }


    /**
     * Test retrieval of selected regions from chr8.
     */
    @Test
    void testFetchSequencesFromChr8() {
        // the first line of chr8 in the test file
        assertThat(instance.fetchSequence("8", 0, 50), is("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN"));
        // random retrieval
        assertThat(instance.fetchSequence("8", 39140, 39150), is("CTCGAGCCCT"));
        // the last line of chr8 in the test file
        assertThat(instance.fetchSequence("8", 49900, 49950), is("attttaggcagatagagaggaaaagaggtccttgggaagtttttgtttat"));
    }


    @Test
    void testFetchSequencesFromVariousChromosomes() {
        assertThat(instance.fetchSequence("7", 44580, 44620), is("AGAGGAGGAAACGTGAATAGTATGCAGCTTCCCGCACACA"));
        assertThat(instance.fetchSequence("X", 42240, 42280), is("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN"));
        assertThat(instance.fetchSequence("Y", 9180, 9230), is("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN"));
        // accessor will also digest chromosome name with prefix
        assertThat(instance.fetchSequence("chrM", 11370, 11420), is("TAGTAAAGATACCTCTTTACGGACTCCACTTATGACTCCCTAAAGCCCAT"));
    }


    @Test
    void testFetchSequenceFromBeyondEndOfContig() {
        // This query asks for one nucleotide past end of chromosome 8
        assertThat(instance.fetchSequence("8", 49950, 49951), is(nullValue()));

        // single nucleotide is located past end of chromosome 8
        assertThat(instance.fetchSequence("8", 49900, 49951), is(nullValue()));
    }


    @Test
    void testFetchSequenceFromNonexistingChromosome() {
        assertThat(instance.fetchSequence("Z", 50, 100), is(nullValue()));
    }


    @Test
    void testSingleFileConstructor() throws Exception {
        GenomeSequenceAccessor accessor = new GenomeSequenceAccessor(FASTA_PATH);
        assertThat(accessor.fetchSequence("7", 44580, 44620), is("AGAGGAGGAAACGTGAATAGTATGCAGCTTCCCGCACACA"));
        accessor.close();
    }
}