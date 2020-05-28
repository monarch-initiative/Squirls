package org.monarchinitiative.threes.core.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = {TestDataSourceConfig.class})
class BinaryClassifierDaoTest {


    @Autowired
    private DataSource dataSource;

    private ClassifierDao dao;

    @BeforeEach
    void setUp() {
        dao = new ClassifierDao(dataSource);
    }

    @Test
    @Sql(scripts = {"create_classifier_table.sql"})
    void storeClassifier() throws Exception {
        final byte[] payload = new byte[]{-128, 6, 0, 88, 127};
        final String version = "v1";
        final int updated = dao.storeClassifier(version, payload);

        byte[] actual = null;
        String actualVersion = null;
        int i = 0;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement ps = connection.prepareStatement("select version, data from SPLICING.CLASSIFIER where version = ?")) {
            ps.setString(1, version);
            final ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                if (i == 0) {
                    actualVersion = rs.getString("version");
                    actual = rs.getBytes("data");
                }
                i++;
            }
        }

        assertThat(i, is(1));
        assertThat(updated, is(1));
        assertThat(actualVersion, is(version));
        assertThat(payload, is(actual));
    }

    @Test
    @Sql(scripts = "create_classifier_table.sql", statements = "insert into SPLICING.CLASSIFIER(version, data) values ('v1', '000F10FF')")
    void readClassifier() {
        final byte[] bytes = dao.readClassifier("v1");
        assertThat(bytes, is(new byte[]{0, 15, 16, -1}));

        final byte[] na = dao.readClassifier("v2");
        assertThat(na, is(nullValue()));
    }

    @Test
    @Sql(scripts = "create_classifier_table.sql",
            statements = "insert into SPLICING.CLASSIFIER(version, data) " +
                    " values ('beef_duet', 'BEEFBEEF'), ('beef_quartet', 'BEEFBEEFBEEFBEEF')")
    void getAllClassifiers() {
        final Collection<String> clfs = dao.getAvailableClassifiers();
        assertThat(clfs, hasSize(2));
        assertThat(clfs, hasItems("beef_duet", "beef_quartet"));
    }
}