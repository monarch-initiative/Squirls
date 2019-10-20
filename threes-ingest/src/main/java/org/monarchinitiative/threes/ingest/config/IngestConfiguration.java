package org.monarchinitiative.threes.ingest.config;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 *
 */
@Configuration
public class IngestConfiguration {

    private final IngestProperties ingestProperties;

    public IngestConfiguration(IngestProperties ingestProperties) {
        this.ingestProperties = ingestProperties;
    }


    @Bean
    public JannovarData jannovarData() throws SerializationException {
        return new JannovarDataSerializer(ingestProperties.getJannovarCachePath().toString()).load();
    }


    @Bean
    public SplicingInformationContentCalculator splicingInformationContentAnnotator(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return new SplicingInformationContentCalculator(splicingPositionalWeightMatrixParser.getDonorMatrix(),
                splicingPositionalWeightMatrixParser.getAcceptorMatrix(),
                splicingPositionalWeightMatrixParser.getSplicingParameters());
    }

    @Bean
    public SplicingPositionalWeightMatrixParser positionalWeightMatrixParser() throws IOException {
        try (InputStream is = Files.newInputStream(ingestProperties.getSplicingInformationContentMatrixPath())) {
            return new InputStreamBasedPositionalWeightMatrixParser(is);
        }
    }
}
