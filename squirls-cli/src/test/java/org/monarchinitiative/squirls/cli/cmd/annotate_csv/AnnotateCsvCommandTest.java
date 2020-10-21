package org.monarchinitiative.squirls.cli.cmd.annotate_csv;

import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.data.JannovarData;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.squirls.cli.TestDataSourceConfig;
import org.monarchinitiative.squirls.cli.data.VariantsForTesting;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestDataSourceConfig.class)
public class AnnotateCsvCommandTest {

    private final Path inputPath = Path.of(AnnotateCsvCommand.class.getResource("test_variants.csv").getPath());
    private final Path outputPath = Path.of("src/test/resources/annotated.csv");

    private final Namespace namespace = new Namespace(Map.of(
            "input", inputPath.toString(),
            "output", outputPath.toString()
    ));
    @Autowired
    public VariantAnnotator annotator;
    @Autowired
    private JannovarData jannovarData;
    @Mock
    private VariantSplicingEvaluator evaluator;

    private AnnotateCsvCommand cmd;


    @BeforeEach
    public void setUp() {
        cmd = new AnnotateCsvCommand(evaluator);
    }

    @AfterEach
    public void tearDown() {
        if (Files.isRegularFile(outputPath)) {
            if (!outputPath.toFile().delete()) {
                System.err.println("Whoops!");
            }
        }
    }

    @Test
    public void run() throws Exception {
        assertThat(outputPath.toFile().exists(), is(false));

        when(evaluator.evaluate("chr1", 21_894_739, "A", "G"))
                .thenReturn(VariantsForTesting.ALPLDonorExon7Minus2(jannovarData.getRefDict(), annotator).getSplicingPredictions());
        when(evaluator.evaluate("chr13", 32_930_748, "T", "G"))
                .thenReturn(VariantsForTesting.BRCA2DonorExon15plus2QUID(jannovarData.getRefDict(), annotator).getSplicingPredictions());

        cmd.run(namespace);

        assertThat(outputPath.toFile().exists(), is(true));

        List<String> lines = new ArrayList<>();
        try (final BufferedReader reader = Files.newBufferedReader(outputPath)) {
            reader.lines().forEach(lines::add);
        }

        assertThat(lines, hasItems(
                "CHROM,POS,REF,ALT,PATHOGENIC,MAX_SCORE,SCORES",
                "chr1,21894739,A,G,true,0.94,NM_000478.4=0.940000",
                "chr13,32930748,T,G,true,0.95,NM_000059.3=0.950000"));
    }
}