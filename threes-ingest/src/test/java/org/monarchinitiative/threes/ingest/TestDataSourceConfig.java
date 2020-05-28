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
import org.monarchinitiative.threes.core.data.ic.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.threes.core.data.ic.SplicingPwmData;
import org.monarchinitiative.threes.core.scoring.calculators.ic.SplicingInformationContentCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessor;
import xyz.ielis.hyperutil.reference.fasta.GenomeSequenceAccessorBuilder;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
//        String jdbcUrl = "jdbc:h2:mem:splicing;INIT=CREATE SCHEMA IF NOT EXISTS SPLICING";
        String jdbcUrl = "jdbc:h2:mem:splicing";
        final HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
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
    public GenomeSequenceAccessor genomeSequenceAccessor() {
        Path fasta = Paths.get(TestDataSourceConfig.class.getResource("chr2chr3_small.fa").getPath());
        Path fastaFai = Paths.get(TestDataSourceConfig.class.getResource("chr2chr3_small.fa.fai").getPath());
        Path fastaDict = Paths.get(TestDataSourceConfig.class.getResource("chr2chr3_small.fa.dict").getPath());
        return GenomeSequenceAccessorBuilder.builder()
                .setFastaPath(fasta)
                .setFastaFaiPath(fastaFai)
                .setFastaDictPath(fastaDict)
                .build();
    }

}
