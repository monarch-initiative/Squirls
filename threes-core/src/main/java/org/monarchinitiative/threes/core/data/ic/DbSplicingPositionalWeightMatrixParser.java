package org.monarchinitiative.threes.core.data.ic;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.threes.core.model.SplicingParameters;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This parser reads PWMs for splice donor, acceptor, and data for splicing parameters from database.
 */
public class DbSplicingPositionalWeightMatrixParser implements SplicingPositionalWeightMatrixParser {

    /**
     * Tolerance when checking that probabilities sum up to 1.
     */
    private static final double EPSILON = 0.004;

    private final SplicingParameters splicingParameters;

    private final DoubleMatrix donorMatrix, acceptorMatrix;

    public DbSplicingPositionalWeightMatrixParser(DataSource dataSource) throws CorruptedPwmException {
        this.donorMatrix = parseMatrix(dataSource, "SPLICE_DONOR_SITE");
        this.acceptorMatrix = parseMatrix(dataSource, "SPLICE_ACCEPTOR_SITE");
        this.splicingParameters = parseSplicingParameters(dataSource);

    }

    private static DoubleMatrix parseMatrix(DataSource dataSource, String pwmName) throws CorruptedPwmException {
        // the outer list always have only 4 elements corresponding to A,C,G,T
        // the inner list represents columns - positions of the sequence
        List<List<Double>> data = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            data.add(new ArrayList<>());
        }

        DoubleMatrix matrix = new DoubleMatrix();
        // TODO - continue
        // matrix
        String sql = String.format("SELECT ROW_IDX, COL_IDX, CELL_VALUE FROM SPLICING.PWM_DATA WHERE PWM_NAME = '%s' " +
                "ORDER BY ROW_IDX ASC, COL_IDX ASC", pwmName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {

                int row = rs.getInt("ROW_IDX");
                double value = rs.getDouble("CELL_VALUE");
                data.get(row).add(value);
            }

        } catch (SQLException e) {
            throw new CorruptedPwmException(e);
        }

        return InputStreamBasedPositionalWeightMatrixParser.mapToDoubleMatrix(data, EPSILON);
    }

    private static SplicingParameters parseSplicingParameters(DataSource dataSource) throws CorruptedPwmException {
        SplicingParameters.Builder builder = SplicingParameters.builder();
        String sql = "SELECT PWM_NAME, PWM_KEY, PWM_VALUE FROM SPLICING.PWM_METADATA";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String name = rs.getString("PWM_NAME");
                String key = rs.getString("PWM_KEY");
                String value = rs.getString("PWM_VALUE");
                outer:
                switch (name) {
                    case "SPLICE_DONOR_SITE":
                        switch (key) {
                            case "EXON":
                                builder.setDonorExonic(Integer.parseInt(value));
                                break outer;
                            case "INTRON":
                                builder.setDonorIntronic(Integer.parseInt(value));
                                break outer;
                            default:
                                throw new CorruptedPwmException(String.format("Unknown key - %s", key));
                        }
                    case "SPLICE_ACCEPTOR_SITE":
                        switch (key) {
                            case "EXON":
                                builder.setAcceptorExonic(Integer.parseInt(value));
                                break outer;
                            case "INTRON":
                                builder.setAcceptorIntronic(Integer.parseInt(value));
                                break outer;
                            default:
                                throw new CorruptedPwmException(String.format("Unknown key - %s", key));
                        }
                    default:
                        throw new CorruptedPwmException(String.format("Unknown name - %s", name));
                }
            }

        } catch (SQLException e) {
            throw new CorruptedPwmException(e);
        }
        return builder.build();
    }

    @Override
    public SplicingParameters getSplicingParameters() {
        return splicingParameters;
    }

    @Override
    public DoubleMatrix getDonorMatrix() {
        return donorMatrix;
    }

    @Override
    public DoubleMatrix getAcceptorMatrix() {
        return acceptorMatrix;
    }
}
