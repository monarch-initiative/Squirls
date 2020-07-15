package org.monarchinitiative.squirls.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import org.monarchinitiative.squirls.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.core.data.ic.SplicingPwmData;
import org.monarchinitiative.squirls.core.data.kmer.FileKMerParser;
import org.monarchinitiative.squirls.core.model.SplicingParameters;
import org.monarchinitiative.squirls.core.reference.allele.AlleleGenerator;
import org.monarchinitiative.squirls.core.reference.transcript.NaiveSplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.reference.transcript.SplicingTranscriptLocator;
import org.monarchinitiative.squirls.core.scoring.DenseSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 *
 */
@Configuration
public class TestDataSourceConfig {

    /**
     * @return in-memory database for testing
     */
    @Bean
    public DataSource dataSource() {
        String jdbcUrl = "jdbc:h2:mem:threes";
        final HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    @Bean
    public SplicingParameters splicingParameters() {
        return PojosForTesting.makeSplicingParameters();
    }

    @Bean
    public ReferenceDictionary referenceDictionary() {
        ReferenceDictionaryBuilder builder = new ReferenceDictionaryBuilder();

        builder.putContigID("chr1", 1);
        builder.putContigName(1, "chr1");
        builder.putContigLength(1, 10_000);

        builder.putContigID("chr2", 2);
        builder.putContigName(2, "chr2");
        builder.putContigLength(2, 100_000);

        builder.putContigID("chr3", 3);
        builder.putContigName(3, "chr3");
        builder.putContigLength(3, 200_000);
        return builder.build();
    }

    @Bean
    public SplicingInformationContentCalculator splicingInformationContentCalculator(SplicingPwmData splicingPwmData) {
        return new SplicingInformationContentCalculator(splicingPwmData);
    }

    @Bean
    public SplicingPwmData splicingPwmData(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return splicingPositionalWeightMatrixParser.getSplicingPwmData();
    }

    @Bean
    public SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser() throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(TestDataSourceConfig.class.getResource("data/ic/spliceSites.yaml").getPath()))) {
            return new InputStreamBasedPositionalWeightMatrixParser(inputStream);
        }
    }

    @Bean
    public AlleleGenerator alleleGenerator(SplicingParameters splicingParameters) {
        return new AlleleGenerator(splicingParameters);
    }

    @Bean
    public SplicingTranscriptLocator splicingTranscriptLocator(SplicingParameters splicingParameters) {
        return new NaiveSplicingTranscriptLocator(splicingParameters);
    }

    @Bean
    public SplicingAnnotator denseSplicingEvaluator(SplicingPwmData splicingPwmData,
                                                    Map<String, Double> hexamerMap,
                                                    Map<String, Double> septamerMap,
                                                    @Qualifier("phylopAccessor") BigWigAccessor phylopAccessor) {
        return new DenseSplicingAnnotator(splicingPwmData, hexamerMap, septamerMap, phylopAccessor);
    }

    @Bean
    public Map<String, Double> hexamerMap() throws IOException {
        Path path = Paths.get(TestDataSourceConfig.class.getResource("data/kmer/hexamer-scores-full.tsv").getPath());
        return new FileKMerParser(path).getKmerMap();
    }

    @Bean
    public Map<String, Double> septamerMap() throws IOException {
        Path path = Paths.get(TestDataSourceConfig.class.getResource("data/kmer/septamer-scores.tsv").getPath());
        return new FileKMerParser(path).getKmerMap();
    }

    @Bean
    public BigWigAccessor phylopAccessor() throws IOException {
        Path path = Paths.get(TestDataSourceConfig.class.getResource("scoring/calculators/conservation/small.bw").getPath());
        return new BigWigAccessor(path);
    }
}
