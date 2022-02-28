package org.monarchinitiative.squirls.io.transcript;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.sgenes.model.Identified;
import org.monarchinitiative.sgenes.model.Transcript;
import org.monarchinitiative.sgenes.simple.Genes;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TranscriptModelServiceSgTest {

    private static final GenomicAssembly ASSEMBLY = GenomicAssemblies.GRCh38p13();

    private TranscriptModelServiceSg service;

    @BeforeEach
    public void setUp() {
        List<Gene> genes = List.of(Genes.surf1(), Genes.surf2());
        service = TranscriptModelServiceSg.of(genes);
    }

    @Test
    public void getTranscriptAccessions() {
        Set<String> accessions = service.getTranscriptAccessions()
                .collect(Collectors.toSet());

        assertThat(accessions, hasItems("ENST00000371974.8", "ENST00000615505.4", "ENST00000371964.5"));
    }

    @Test
    public void genes() {
        List<Gene> genes = service.genes().collect(Collectors.toList());

        Set<String> geneAccessions = genes.stream().map(Identified::accession).collect(Collectors.toSet());
        assertThat(geneAccessions, hasItems("ENSG00000148290.10", "ENSG00000148291.10"));

        Set<String> symbols = genes.stream().map(Identified::symbol).collect(Collectors.toSet());
        assertThat(symbols, hasItems("SURF1", "SURF2"));
    }

    @ParameterizedTest
    @CsvSource({
            "ENST00000371974.8, true, SURF1-201",
            "ENST00000615505.4, true, SURF1-205",
            "ENST00000371964.5, true, SURF2-201",
            // missing
            "ENST00000371975.8, false, ''",
    })
    public void transcriptByAccession(String txAccession, boolean expected, String symbol) {
        Optional<Transcript> txOpt = service.transcriptByAccession(txAccession);
        assertThat(txOpt.isPresent(), equalTo(expected));

        if (expected) {
            Transcript tx = txOpt.get();
            assertThat(tx.accession(), equalTo(txAccession));
            assertThat(tx.symbol(), equalTo(symbol));
        }
    }


    @ParameterizedTest
    @CsvSource({
            // SURF1 span: chr9, 133,351,757 - 133,356,487
            // SURF2 span: chr9, 133,356,549 - 133,361,158
            "chr9, 133351756, 133351757, ''", // Upstream of SURF1 5'
            "chr9, 133351757, 133351758, 'ENST00000371974.8'", // SURF1 5'
            "chr9, 133356486, 133356487, 'ENST00000371974.8'", // SURF1 3'
            "chr9, 133356487, 133356488, ''", // Downstream of SURF1 3'

            "chr9, 133356549, 133356550, ''", // Upstream of SURF2 5'
            "chr9, 133356550, 133356551, 'ENST00000371964.5'", // SURF2 5'
            "chr9, 133361157, 133361158, 'ENST00000371964.5'", // SURF2 3'
            "chr9, 133361158, 133361159, ''", // Downstream of SURF2 3'


            "chr9, 133351760, 133361100, 'ENST00000371964.5;ENST00000371974.8;ENST00000371974.8'", // spanning both genes
    })
    public void overlappingTranscripts(String contig, int start, int end, String accessions) {
        GenomicRegion query = GenomicRegion.of(ASSEMBLY.contigByName(contig), Strand.POSITIVE, CoordinateSystem.zeroBased(), start, end);
        List<Transcript> transcripts = service.overlappingTranscripts(query);
        Set<String> actual = transcripts.stream()
                .map(Identified::accession)
                .collect(Collectors.toSet());

        String[] expected = prepareAccessions(accessions);
        assertThat(actual, hasItems(expected));
    }

    private static String[] prepareAccessions(String accessions) {
        String[] split = accessions.split(";");
        if (split.length == 1 && Objects.equals(split[0], ""))
            return new String[0];
        return split;
    }
}