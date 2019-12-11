package org.monarchinitiative.threes.cli.cmd.annotate_pos;

import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.monarchinitiative.threes.cli.TestDataSourceConfig;
import org.monarchinitiative.threes.core.scoring.SplicingPathogenicityData;
import org.monarchinitiative.threes.core.scoring.VariantSplicingEvaluator;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestDataSourceConfig.class)
class AnnotatePosCommandTest {

    @Mock
    private VariantSplicingEvaluator evaluator;

    private AnnotatePosCommand cmd;

    @BeforeEach
    void setUp() {
        cmd = new AnnotatePosCommand(evaluator);
    }

    @Test
    void run() throws Exception {
        when(evaluator.evaluate("chr1", 123, "C", "T")).thenReturn(Map.of(
                "one", SplicingPathogenicityData.builder()
                        .putScore("alpha", .2)
                        .putScore("beta", .4)
                        .build(),
                "two", SplicingPathogenicityData.builder()
                        .putScore("alpha", 1.2)
                        .putScore("beta", 1.4)
                        .build()));
        when(evaluator.evaluate("chrX", 456, "G", "A")).thenReturn(Map.of(
                "three", SplicingPathogenicityData.builder()
                        .putScore("alpha", 3.2)
                        .putScore("beta", 3.4)
                        .build(),
                "four", SplicingPathogenicityData.builder()
                        .putScore("alpha", 4.2)
                        .putScore("beta", 4.4)
                        .build()));

        Namespace namespace = new Namespace(Map.of("change", List.of("chr1:123C>T", "chrX:456G>A")));
        cmd.run(namespace);
    }
}