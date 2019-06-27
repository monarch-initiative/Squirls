package org.monarchinitiative.threes.core.calculators.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"file:src/test/resources/sql/create_septamers_table.sql", "file:src/test/resources/sql/insert_septamer_data.sql"})
class DbSmsDaoTest {

    @Autowired
    private DataSource dataSource;

    private DbSmsDao dao;

    @BeforeEach
    void setUp() {
        dao = new DbSmsDao(dataSource);
    }

    @Test
    void fetchData() {
        Map<String, Double> septamers = dao.getSeptamerMap();

        assertThat(septamers.size(), is(6));
        assertThat(septamers, hasEntry("AAAAAAA", 0.1));
        assertThat(septamers, hasEntry("AAAAAAC", 0.2));
        assertThat(septamers, hasEntry("AAAAAAG", 0.3));
        assertThat(septamers, hasEntry("AAAAAAT", 0.4));
        assertThat(septamers, hasEntry("AAAAACA", 0.5));
        assertThat(septamers, hasEntry("AAAAACC", 0.6));
    }
}