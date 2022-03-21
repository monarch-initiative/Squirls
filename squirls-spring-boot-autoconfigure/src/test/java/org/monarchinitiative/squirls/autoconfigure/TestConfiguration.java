package org.monarchinitiative.squirls.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class TestConfiguration {

    @Bean
    public Path squirlsDataDirectory(Environment environment) {
        String property = environment.getProperty("squirls.data-directory");
        return Path.of(property);
    }

}
