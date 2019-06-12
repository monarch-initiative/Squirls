package org.monarchinitiative.sss.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataDirectoryAutoConfigurationTest extends AbstractAutoConfigurationTest {


    @Test
    public void testUndefinedDataPath() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(DataDirectoryAutoConfiguration.class));
        assertThat(thrown.getMessage(), containsString("Three S data directory not defined. Please provide a valid path"));
    }


    @Test
    public void testEmptyDataPath() {
        Throwable thrown = assertThrows(BeanCreationException.class, () ->
                load(DataDirectoryAutoConfiguration.class, "sss.data-directory=")
        );
        assertThat(thrown.getMessage(), containsString("Three S data directory not defined. Please provide a valid path"));
    }

    @Test
    public void testDataPath() {
        load(DataDirectoryAutoConfiguration.class, "sss.data-directory=" + TEST_DATA);
        Path threeSDataDirectory = (Path) this.ctx.getBean("threeSDataDirectory");
        assertThat(threeSDataDirectory.getFileName(), equalTo(Paths.get("local_data")));
    }
}