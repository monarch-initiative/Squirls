package org.monarchinitiative.threes.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import org.monarchinitiative.threes.core.reference.GenomeCoordinatesFlipper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Configuration
public class TestDataSourceConfig {


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
    public GenomeCoordinatesFlipper genomeCoordinatesFlipper(ReferenceDictionary referenceDictionary) {
        final Map<String, Integer> contigLengths = referenceDictionary.getContigIDToName().keySet().stream()
                .collect(Collectors.toMap(idx -> referenceDictionary.getContigIDToName().get(idx),
                        idx -> referenceDictionary.getContigIDToLength().get(idx)));
        return new GenomeCoordinatesFlipper(contigLengths);
    }

}
