package org.monarchinitiative.squirls.core.data.kmer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"create_kmers_table.sql", "insert_hexamer_data.sql", "insert_septamer_data.sql"})
class DbKMerDaoTest {

    @Autowired
    private DataSource dataSource;

    private DbKMerDao dao;

    @BeforeEach
    void setUp() {
        dao = new DbKMerDao(dataSource);
    }

    @Test
    void getSeptamerMap() {
        Map<String, Double> septamers = dao.getSeptamerMap();

        assertThat(septamers.size(), is(6));
        assertThat(septamers, hasEntry("AAAAAAA", 0.1));
        assertThat(septamers, hasEntry("AAAAAAC", 0.2));
        assertThat(septamers, hasEntry("AAAAAAG", 0.3));
        assertThat(septamers, hasEntry("AAAAAAT", 0.4));
        assertThat(septamers, hasEntry("AAAAACA", 0.5));
        assertThat(septamers, hasEntry("AAAAACC", 0.6));
    }

    @Test
    void getHexamerMap() {
        Map<String, Double> septamers = dao.getHexamerMap();

        assertThat(septamers.size(), is(6));
        assertThat(septamers, hasEntry("AAAAAA", 0.1));
        assertThat(septamers, hasEntry("AAAAAC", 0.2));
        assertThat(septamers, hasEntry("AAAAAG", 0.3));
        assertThat(septamers, hasEntry("AAAAAT", 0.4));
        assertThat(septamers, hasEntry("AAAACA", 0.5));
        assertThat(septamers, hasEntry("AAAACC", 0.6));
    }
}