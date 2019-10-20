package org.monarchinitiative.threes.ingest.pwm;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.threes.ingest.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"file:src/test/resources/sql/create_schema.sql", "file:src/test/resources/sql/create_pwm_tables.sql"})
class PwmIngestDaoTest {

    @Autowired
    private DataSource dataSource;

    private PwmIngestDao dao;

    @BeforeEach
    void setUp() {
        dao = new PwmIngestDao(dataSource);
    }

    @Test
    void insertDoubleMatrix() throws Exception {
        DoubleMatrix data = new DoubleMatrix(new double[][]{
                {.1, .2},
                {.3, .4}
        });
        int nRows = dao.insertDoubleMatrix(data, "yeah", 1, 1);
        assertThat(nRows, is(6));

        List<String> dataLines = new ArrayList<>();
        List<String> metaDataLines = new ArrayList<>();

        String pwmDataSql = "select PWM_NAME, ROW_IDX, COL_IDX, CELL_VALUE from SPLICING.PWM_DATA";
        String pwmMetadataSql = "select PWM_NAME, PWM_KEY, PWM_VALUE from SPLICING.PWM_METADATA";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement dataStatement = connection.prepareStatement(pwmDataSql);
             PreparedStatement metadataStatement = connection.prepareStatement(pwmMetadataSql)) {
            final ResultSet dataRs = dataStatement.executeQuery();
            while (dataRs.next()) {
                String line = String.join("\t",
                        dataRs.getString("PWM_NAME"),
                        dataRs.getString("ROW_IDX"),
                        dataRs.getString("COL_IDX"),
                        dataRs.getString("CELL_VALUE"));
                dataLines.add(line);
            }


            final ResultSet metaRs = metadataStatement.executeQuery();
            while (metaRs.next()) {
                String line = String.join("\t",
                        metaRs.getString("PWM_NAME"),
                        metaRs.getString("PWM_KEY"),
                        metaRs.getString("PWM_VALUE"));
                metaDataLines.add(line);
            }
        }

        assertThat(dataLines, hasSize(4));
        assertThat(dataLines, hasItems(
                String.join("\t", "yeah", "0", "0", "0.1"),
                String.join("\t", "yeah", "0", "1", "0.2"),
                String.join("\t", "yeah", "1", "0", "0.3"),
                String.join("\t", "yeah", "1", "1", "0.4")));

        assertThat(metaDataLines, hasSize(2));
        assertThat(metaDataLines, hasItems(
                String.join("\t", "yeah", "EXON", "1"),
                String.join("\t", "yeah", "INTRON", "1")));
    }
}