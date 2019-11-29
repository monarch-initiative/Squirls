package org.monarchinitiative.threes.ingest.transcripts;

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class JannovarDataManagerTest {

    private final Path TEST_DBS = Paths.get(JannovarDataManagerTest.class.getResource("hg19").getPath());

    @Test
    void fromDirectory() {
        JannovarDataManager jannovarDataManager = JannovarDataManager.fromDirectory(TEST_DBS);
        final Map<String, List<TranscriptModel>> txsByGene = jannovarDataManager.getAllTranscriptModels().stream()
                .collect(Collectors.groupingBy(TranscriptModel::getGeneSymbol));
        assertThat(txsByGene.keySet(), hasItems("HNF4A", "GCK", "FBN1"));
        assertThat(txsByGene.get("HNF4A"), hasSize(24));
        assertThat(txsByGene.get("GCK"), hasSize(14));
        assertThat(txsByGene.get("FBN1"), hasSize(11));
    }
}