package org.monarchinitiative.sss.core.pwm;

import org.jblas.DoubleMatrix;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.sss.core.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {TestDataSourceConfig.class})
@Sql(scripts = {"file:src/test/resources/sql/create_pwm_tables.sql", "file:src/test/resources/sql/insert_pwm_data.sql"})
class DbSplicingPositionalWeightMatrixParserTest {

    @Autowired
    private DataSource dataSource;

    private DbSplicingPositionalWeightMatrixParser parser;


    @Test
    void donorAcceptorAndSplicingParametersAreParsed() {
        parser = new DbSplicingPositionalWeightMatrixParser(dataSource);

        // ----------        SPLICING PARAMETERS --------
        SplicingParameters sp = parser.getSplicingParameters();
        assertThat(sp, is(SplicingParameters.builder()
                .setDonorExonic(3)
                .setDonorIntronic(2)
                .setAcceptorExonic(1)
                .setAcceptorIntronic(2)
                .build()));

        // ----------        DONOR         --------
        DoubleMatrix donorMatrix = parser.getDonorMatrix();
        assertThat(donorMatrix.columns, is(5));
        assertThat(donorMatrix.rows, is(4));
        DoubleMatrix donor = new DoubleMatrix(4, 5);
        donor.put(0, 0, 0.25);
        donor.put(1, 0, 0.25);
        donor.put(2, 0, 0.25);
        donor.put(3, 0, 0.25);

        donor.put(0, 1, 0.30);
        donor.put(1, 1, 0.20);
        donor.put(2, 1, 0.10);
        donor.put(3, 1, 0.40);

        donor.put(0, 2, 0.15);
        donor.put(1, 2, 0.25);
        donor.put(2, 2, 0.30);
        donor.put(3, 2, 0.30);

        donor.put(0, 3, 0.10);
        donor.put(1, 3, 0.10);
        donor.put(2, 3, 0.20);
        donor.put(3, 3, 0.60);

        donor.put(0, 4, 0.15);
        donor.put(1, 4, 0.25);
        donor.put(2, 4, 0.30);
        donor.put(3, 4, 0.30);

        assertThat(donorMatrix, is(donor));

        // ----------        ACCEPTOR      --------
        DoubleMatrix acceptorMatrix = parser.getAcceptorMatrix();
        assertThat(acceptorMatrix.columns, is(3));
        assertThat(acceptorMatrix.rows, is(4));

        DoubleMatrix acceptor = new DoubleMatrix(4, 3);
        acceptor.put(0, 0, 0.10);
        acceptor.put(1, 0, 0.90);
        acceptor.put(2, 0, 0.00);
        acceptor.put(3, 0, 0.00);

        acceptor.put(0, 1, 0.05);
        acceptor.put(1, 1, 0.15);
        acceptor.put(2, 1, 0.25);
        acceptor.put(3, 1, 0.55);

        acceptor.put(0, 2, 0.10);
        acceptor.put(1, 2, 0.20);
        acceptor.put(2, 2, 0.40);
        acceptor.put(3, 2, 0.30);

        assertThat(acceptorMatrix, is(acceptor));

    }
}