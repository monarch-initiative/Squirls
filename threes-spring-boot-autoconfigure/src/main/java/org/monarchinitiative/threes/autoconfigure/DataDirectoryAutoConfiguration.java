package org.monarchinitiative.threes.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Daniel Danis <daniel.danis@jax.org>
 */
@Configuration
@EnableConfigurationProperties(ThreeSProperties.class)
public class DataDirectoryAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataDirectoryAutoConfiguration.class);

    private final ThreeSProperties properties;

    public DataDirectoryAutoConfiguration(ThreeSProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "threeSDataDirectory")
    public Path threeSDataDirectory() {
        String dataDirectory = properties.getDataDirectory();
        if (dataDirectory == null || dataDirectory.isEmpty()) {
            throw new UndefinedDataDirectoryException("Three S data directory not defined. Please provide a valid path");
        }
        Path dataPath = Paths.get(dataDirectory).toAbsolutePath();
        LOGGER.info("Three S data directory set to '{}'", dataPath);
        return dataPath;
    }
}
