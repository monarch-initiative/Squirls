package org.monarchinitiative.threes.ingest;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.reference.TranscriptModelBuilder;
import org.monarchinitiative.threes.core.calculators.ic.SplicingInformationContentCalculator;
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.monarchinitiative.threes.core.reference.fasta.GenomeSequenceAccessor;
import org.monarchinitiative.threes.core.reference.fasta.InvalidFastaFileException;
import org.monarchinitiative.threes.core.reference.fasta.PrefixHandlingGenomeSequenceAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Configuration
public class TestDataSourceConfig {


    @Bean
    public SplicingPwmData splicingPwmData() {
        return SplicingPwmData.builder()
                .setDonor(PojosForTesting.makeDonorMatrix())
                .setAcceptor(PojosForTesting.makeAcceptorMatrix())
                .setParameters(PojosForTesting.makeSplicingParameters())
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * @return in-memory database for testing
     */
    @Bean
    public DataSource dataSource() {
        String jdbcUrl = "jdbc:h2:mem:splicing;INIT=CREATE SCHEMA IF NOT EXISTS SPLICING";
        final HikariConfig config = new HikariConfig();
        config.setUsername("sa");
//        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    @Bean
    public ReferenceDictionary referenceDictionary() {
        ReferenceDictionaryBuilder builder = new ReferenceDictionaryBuilder();

        builder.putContigID("chr2", 2);
        builder.putContigName(2, "chr2");
        builder.putContigLength(2, 100_000);

        builder.putContigID("chr3", 3);
        builder.putContigName(3, "chr3");
        builder.putContigLength(3, 200_000);
        return builder.build();
    }

    @Bean
    public List<TranscriptModel> transcriptModels(ReferenceDictionary referenceDictionary) {
        List<TranscriptModel> models = new ArrayList<>();

        // adam
        final TranscriptModelBuilder adam = new TranscriptModelBuilder();
        adam.setGeneSymbol("ADAM");
        adam.setAccession("adam");
        adam.setStrand(Strand.FWD);
        adam.setTXRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 20_000));
        adam.setCDSRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 11_000, 19_000));
        adam.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 10_000, 12_000));
        adam.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 14_000, 16_000));
        adam.addExonRegion(new GenomeInterval(referenceDictionary, Strand.FWD, 2, 18_000, 20_000));
        models.add(adam.build());

        return models;
    }

    @Bean
    public Map<String, Integer> contigLengthMap(ReferenceDictionary referenceDictionary) {
        return referenceDictionary.getContigIDToName().keySet().stream()
                .collect(Collectors.toMap(
                        id -> referenceDictionary.getContigIDToName().get(id), // key - chromosome number
                        id -> referenceDictionary.getContigIDToLength().get(id))); // value - chromosome length
    }

    @Bean
    public GenomeCoordinatesFlipper genomeCoordinatesFlipper(Map<String, Integer> contigLengthMap) {
        return new GenomeCoordinatesFlipper(contigLengthMap);
    }

    @Bean
    public SplicingInformationContentCalculator splicingInformationContentCalculator(SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser) {
        return new SplicingInformationContentCalculator(splicingPositionalWeightMatrixParser.getSplicingPwmData());
    }

    @Bean
    public SplicingPositionalWeightMatrixParser splicingPositionalWeightMatrixParser() throws Exception {
        try (InputStream is = TestDataSourceConfig.class.getResourceAsStream("spliceSites.yaml")) {
            return new InputStreamBasedPositionalWeightMatrixParser(is);
        }
    }

    @Bean
    public JannovarData jannovarData(ReferenceDictionary referenceDictionary, List<TranscriptModel> transcriptModels) {
        return new JannovarData(referenceDictionary, ImmutableList.copyOf(transcriptModels));
    }

    /**
     * This accessor operates on small FASTA file that contains these regions:
     * - chr2:1-100_000
     * - chr3:1-200_000
     * <p>
     * The names of contigs are `chr2`, and `chr3`, though
     */
    @Bean
    public GenomeSequenceAccessor genomeSequenceAccessor() throws URISyntaxException, InvalidFastaFileException {
        Path fasta = Paths.get(TestDataSourceConfig.class.getResource("chr2chr3_small.fa").toURI());
        Path fastaIdx = Paths.get(TestDataSourceConfig.class.getResource("chr2chr3_small.fa.fai").toURI());
        return new PrefixHandlingGenomeSequenceAccessor(fasta, fastaIdx);
    }

}
