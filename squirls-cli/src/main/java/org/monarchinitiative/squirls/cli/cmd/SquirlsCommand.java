package org.monarchinitiative.squirls.cli.cmd;

import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.cli.visualization.panel.PanelGraphicsGenerator;
import org.monarchinitiative.squirls.cli.visualization.selector.SimpleVisualizationContextSelector;
import org.monarchinitiative.squirls.cli.writers.ResultWriterFactory;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.vmvt.core.VmvtGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Configuration
@EnableAutoConfiguration
public abstract class SquirlsCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-c", "--config"},
            paramLabel = "squirls-config.yml",
            description = "path to configuration file generated by the `generate-config` command")
    public Path configFile;


    protected ConfigurableApplicationContext getContext() {
        // bootstrap Spring application context
        return new SpringApplicationBuilder(SquirlsCommand.class)
                .properties(Map.of("spring.config.location", configFile.toString()))
                .run();
    }

    /**
     * Process predictions for transcripts into a single record in format <code>NM_123456.7=0.88;ENST00000123456.5=0.99</code>
     *
     * @param predictionData map with predictions
     * @return record
     */
    protected static String processScores(Map<String, SplicingPredictionData> predictionData) {
        return predictionData.keySet().stream()
                .sorted()
                .map(tx -> String.format("%s=%f", tx, predictionData.get(tx).getPrediction().getMaxPathogenicity()))
                .collect(Collectors.joining(";"));
    }

    @Bean
    public SplicingVariantGraphicsGenerator splicingVariantGraphicsGenerator(SplicingPwmData splicingPwmData) {
        //        return new SimpleSplicingVariantGraphicsGenerator(splicingPwmData);

        final VmvtGenerator generator = new VmvtGenerator();
        final SimpleVisualizationContextSelector selector = new SimpleVisualizationContextSelector();
        return new PanelGraphicsGenerator(generator, splicingPwmData, selector);
    }

    @Bean
    public ResultWriterFactory resultWriterFactory(SplicingVariantGraphicsGenerator splicingVariantGraphicsGenerator) {
        return new ResultWriterFactory(splicingVariantGraphicsGenerator);
    }
}
