package org.monarchinitiative.threes.core.reference.fasta;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.model.GenomeCoordinates;
import org.monarchinitiative.threes.core.model.SequenceInterval;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PrefixHandlingGenomeSequenceAccessorTest {

    private static Path FASTA_PATH;

    private static Path FASTA_IDX_PATH;

    private PrefixHandlingGenomeSequenceAccessor instance;


    @BeforeAll
    static void setUpBefore() throws Exception {
        /*
        This FASTA file contains first 49950 nucleotides of all canonical chromosomes. Chromosomes are labeled '1, 2, ..., 22, X, Y, M'.
         */
        FASTA_PATH = Paths.get(PrefixHandlingGenomeSequenceAccessor.class.getResource("shortHg19.fa").toURI());
        FASTA_IDX_PATH = Paths.get(PrefixHandlingGenomeSequenceAccessor.class.getResource("shortHg19.fa.fai").toURI());
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
                .setCoordinates(GenomeCoordinates.newBuilder()
                        .setContig("chr2")
                        .setBegin(10000)
                        .setEnd(10100)
                        .setStrand(true)
                        .build())
                .setSequence("CGTATCCcacacaccacacccacacaccacacccacacacacccacacccacacccacacacaccacacccacacaccacacccacacccacacaccaca")
                .build()));
    }

}