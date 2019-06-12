package org.monarchinitiative.sss.autoconfigure;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractAutoConfigurationTest {

    protected static final Path TEST_DATA = Paths.get("src/test/resources/local_data");

    protected static final String TEST_DATA_ENV = "sss.data-directory=" + TEST_DATA;

    protected ConfigurableApplicationContext ctx;

    @AfterEach
    public void closeContext() {
        if (this.ctx != null) {
            this.ctx.close();
        }
    }

    protected void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(config);
        TestPropertyValues.of(environment)
                .applyTo(ctx);
        ctx.refresh();
        this.ctx = ctx;
    }

}
