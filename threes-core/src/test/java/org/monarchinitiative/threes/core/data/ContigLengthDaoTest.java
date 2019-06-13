package org.monarchinitiative.threes.core.data;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"file:src/test/resources/sql/create_contig_lengths.sql",
        "file:src/test/resources/sql/insert_contig_lengths.sql"})
class ContigLengthDaoTest {

    @Autowired
    private DataSource dataSource;

    private ContigLengthDao dao;

    @BeforeEach
    void setUp() {
        dao = new ContigLengthDao(dataSource);
    }

    @Test
    void basic() {
        final Map<String, Integer> contigLengths = dao.getContigLengths();
        assertThat(contigLengths, instanceOf(ImmutableMap.class));

        assertThat(contigLengths, hasEntry("chr1", 10000));
        assertThat(contigLengths, hasEntry("chr2", 20000));
        assertThat(contigLengths, hasEntry("chrX", 30000));
        assertThat(contigLengths.size(), is(3));
    }
}