package org.monarchinitiative.squirls.cli.cmd.annotate_pos;


import org.monarchinitiative.squirls.cli.Main;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static picocli.CommandLine.Parameters;

@CommandLine.Command(name = "annotate-pos",
        aliases = {"P"},
        header = "Annotate several variant positions",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
public class AnnotatePosCommand extends SquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatePosCommand.class);

    private static final String DELIMITER = "\t";

    @Parameters(arity = "1..*",
            paramLabel = "chr3:165504107A>C",
            description = "nucleotide change(s) to annotate")
    public List<String> rawChanges;

    @Override
    public Integer call() throws Exception {
        try (final ConfigurableApplicationContext context = getContext()) {
            LOGGER.info("Changes: {}", rawChanges);
            final VariantSplicingEvaluator splicingEvaluator = context.getBean(VariantSplicingEvaluator.class);

            // parse changes (input)
            final List<VariantChange> changes = rawChanges.stream()
                    .map(VariantChange::fromString)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toUnmodifiableList());

            System.out.println();
            for (VariantChange change : changes) {
                Map<String, SplicingPredictionData> predictionData = splicingEvaluator.evaluate(change.getContig(), change.getPos(), change.getRef(), change.getAlt());
                List<String> columns = new ArrayList<>();

                // variant
                columns.add(change.getVariantChange());

                // is pathogenic
                boolean isPathogenic = predictionData.values().stream()
                        .anyMatch(spd -> spd.getPrediction().isPositive());
                columns.add(isPathogenic ? "pathogenic" : "neutral");

                // max pathogenicity
                double maxScore = predictionData.values().stream()
                        .mapToDouble(e -> e.getPrediction().getMaxPathogenicity())
                        .max()
                        .orElse(Double.NaN);
                columns.add(String.format("%.3f", maxScore));

                // predictions per transcript
                final String scores = processScores(predictionData);
                columns.add(scores);

                System.out.println(String.join(DELIMITER, columns));
            }
            System.out.println();
        }

        return 0;
    }
}
