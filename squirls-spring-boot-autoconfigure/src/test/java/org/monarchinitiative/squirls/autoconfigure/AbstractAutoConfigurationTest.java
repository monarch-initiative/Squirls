package org.monarchinitiative.squirls.autoconfigure;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public abstract class AbstractAutoConfigurationTest {

    protected static final Path TEST_DATA = Paths.get("src/test/resources/data");

    protected ConfigurableApplicationContext context;

    @AfterEach
    public void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    protected void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(config);
        TestPropertyValues.of(environment)
                .applyTo(ctx);
        ctx.refresh();
        this.context = ctx;
    }
}
