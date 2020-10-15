package org.monarchinitiative.squirls.cli;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.squirls.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.data.kmer.FileKMerParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Configuration
public class TestDataSourceConfig {


    /**
     * Small Jannovar cache containing RefSeq transcripts of several genes only:
     * <ul>
     *     <li>SURF1</li>
     *     <li>SURF2</li>
     *     <li>ALPL</li>
     *     <li>TSC2</li>
     *     <li>COL4A5</li>
     *     <li>CYP17A1</li>
     *     <li>HBB</li>
     *     <li>NF1</li>
     *     <li>CFTR</li>
     *     <li>BRCA2</li>
     *     <li>VWF</li>
     *     <li>RYR1</li>
     * </ul>
     * <p>
     *     The small cache was created from Jannovar v0.29 refseq cache using Jannovar Sieve app.
     * </p>
     *
     * @return {@link JannovarData} cache
     */
    @Bean
    public JannovarData jannovarData() throws SerializationException {
        return new JannovarDataSerializer(TestDataSourceConfig.class.getResource("small_refseq.ser").getFile()).load();
    }

    @Bean
    public ReferenceDictionary referenceDictionary(JannovarData jannovarData) {
        return jannovarData.getRefDict();
    }

    @Bean
    public SplicingPwmData splicingPwmData() throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(TestDataSourceConfig.class.getResource("spliceSites.yaml").getPath()))) {
            final InputStreamBasedPositionalWeightMatrixParser parser = new InputStreamBasedPositionalWeightMatrixParser(is);
            return parser.getSplicingPwmData();
        }
    }

    @Bean
    public Map<String, Double> hexamerMap() throws IOException {
        Path path = Paths.get(TestDataSourceConfig.class.getResource("hexamer-scores-full.tsv").getPath());
        return new FileKMerParser(path).getKmerMap();
    }

    @Bean
    public Map<String, Double> septamerMap() throws IOException {
        Path path = Paths.get(TestDataSourceConfig.class.getResource("septamer-scores.tsv").getPath());
        return new FileKMerParser(path).getKmerMap();
    }
}
