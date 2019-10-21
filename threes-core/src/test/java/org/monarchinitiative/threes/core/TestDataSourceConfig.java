package org.monarchinitiative.threes.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import org.monarchinitiative.threes.core.calculators.sms.SMSCalculator;
import org.monarchinitiative.threes.core.data.sms.FileSMSParser;
import org.monarchinitiative.threes.core.data.sms.SMSParser;
import org.monarchinitiative.threes.core.model.SplicingParameters;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
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

        builder.putContigID("chr2", 2);
        builder.putContigName(2, "chr2");
        builder.putContigLength(2, 100_000);

        builder.putContigID("chr3", 3);
        builder.putContigName(3, "chr3");
        builder.putContigLength(3, 200_000);
        return builder.build();
    }


    @Bean
    public GenomeCoordinatesFlipper genomeCoordinatesFlipper(Map<String, Integer> contigLengthMap) {
        return new GenomeCoordinatesFlipper(contigLengthMap);
    }

    @Bean
    public SMSCalculator smsCalculator(SMSParser smsParser) {
        return new SMSCalculator(smsParser.getSeptamerMap());
    }

    @Bean
    public SMSParser smsParser() throws IOException {
        Path path = Paths.get(TestDataSourceConfig.class.getResource("calculators/sms/septamer-scores.tsv").getPath());
        return new FileSMSParser(path);
    }
}
