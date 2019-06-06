package org.monarchinitiative.sss.core.reference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.sss.core.model.GenomeInterval;
import org.monarchinitiative.sss.core.model.SequenceInterval;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PrefixHandlingGenomeSequenceAccessorTest {

    private static File FASTA_PATH;

    private static File FASTA_IDX_PATH;

    private PrefixHandlingGenomeSequenceAccessor instance;


    @BeforeAll
    static void setUpBefore() {
        /*
        This FASTA file contains first 49950 nucleotides of all canonical chromosomes. Chromosomes are labeled '1, 2, ..., 22, X, Y, M'.
         */
        FASTA_PATH = new File(PrefixHandlingGenomeSequenceAccessor.class.getResource("shortHg19.fa").getFile());
        FASTA_IDX_PATH = new File(PrefixHandlingGenomeSequenceAccessor.class.getResource("shortHg19.fa.fai").getFile());
    }


    @BeforeEach
    void setUp() throws InvalidFastaFileException {
        instance = new PrefixHandlingGenomeSequenceAccessor(FASTA_PATH, FASTA_IDX_PATH);
    }


    @AfterEach
    void tearDown() throws Exception {
        instance.close();
    }

    @Test
    void fetchSequenceFromPrefixedChromosome() throws Exception {
        SequenceInterval si = instance.fetchSequence("chr2", 10000, 10100, true);
        assertThat(si, is(SequenceInterval.newBuilder()
                .setInterval(GenomeInterval.newBuilder()
                        .setContig("chr2")
                        .setBegin(10000)
                        .setEnd(10100)
                        .setStrand(true)
                        .setContigLength(49950)
                        .build())
                .setSequence("CGTATCCcacacaccacacccacacaccacacccacacacacccacacccacacccacacacaccacacccacacaccacacccacacccacacaccaca")
                .build()));
    }

}