package org.monarchinitiative.squirls.core.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {TestDataSourceConfig.class})
public class DbClassifierDataManagerTest {

    private static final double EPSILON = 5E-12;

    @Autowired
    public DataSource dataSource;

    private DbClassifierDataManager manager;

    @BeforeEach
    public void setUp() {
        manager = new DbClassifierDataManager(dataSource);
    }

    @Test
    @Sql(scripts = {"create_classifier_table.sql"})
    public void storeClassifier() throws Exception {
        final byte[] payload = new byte[]{-128, 6, 0, 88, 127};
        final String version = "v1";
        final int updated = manager.storeClassifier(version, payload);

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
    public void readClassifier() {
        final Optional<byte[]> bytes = manager.readClassifierBytes("v1");
        assertTrue(bytes.isPresent());
        assertThat(bytes.get(), is(new byte[]{0, 15, 16, -1}));

        final Optional<byte[]> na = manager.readClassifierBytes("v2");
        assertTrue(na.isEmpty());
    }

    @Test
    @Sql(scripts = "create_classifier_table.sql",
            statements = "insert into SPLICING.CLASSIFIER(version, data) " +
                    " values ('beef_duet', 'BEEFBEEF'), ('beef_quartet', 'BEEFBEEFBEEFBEEF')")
    public void getAllClassifiers() {
        final Collection<String> clfs = manager.getAvailableClassifiers();
        assertThat(clfs, hasSize(2));
        assertThat(clfs, hasItems("beef_duet", "beef_quartet"));
    }

    @Test
    public void jsonify() {
        final Map<String, Double> parameters = Map.of("bla", 0.123456789012, "kva", 11.998877665544);
        final String payload = DbClassifierDataManager.jsonify(parameters);
        assertThat(payload, is("{\"bla\": 0.123456789012, \"kva\": 11.998877665544}"));
    }

    @Test
    public void deJsonify() {
        String payload = "{\"bla\": 0.123456789012, \"kva\": 11.998877665544}";
        final Map<String, Double> params = DbClassifierDataManager.deJsonify(payload);

        assertThat(params.size(), is(2));
        assertThat(params.keySet(), hasItems("bla", "kva"));
        assertThat(params.get("bla"), is(closeTo(0.123456789012, EPSILON)));
        assertThat(params.get("kva"), is(closeTo(11.998877665544, EPSILON)));
    }
}