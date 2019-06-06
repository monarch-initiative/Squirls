package org.monarchinitiative.sss.core.pwm;

import org.jblas.DoubleMatrix;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DbSplicingPositionalWeightMatrixParser implements SplicingPositionalWeightMatrixParser {

    /**
     * Tolerance when checking that probabilities sum up to 1.
     */
    private static final double EPSILON = 0.004;

    private final SplicingParameters splicingParameters;

    private final DoubleMatrix donorMatrix, acceptorMatrix;

    public DbSplicingPositionalWeightMatrixParser(DataSource dataSource) throws CorruptedPwmException {
        this.splicingParameters = parseSplicingParameters(dataSource);
        this.donorMatrix = parseMatrix(dataSource, "SPLICE_DONOR_SITE");
        this.acceptorMatrix = parseMatrix(dataSource, "SPLICE_ACCEPTOR_SITE");

    }

    private static DoubleMatrix parseMatrix(DataSource dataSource, String pwmName) throws CorruptedPwmException {
        List<List<Double>> data = new ArrayList<>();
        String sql = String.format("SELECT ROW_IDX, COL_IDX, CELL_VALUE FROM SPLICING.PWM_DATA WHERE PWM_NAME = %s", pwmName);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int row = rs.getInt("ROW_IDX");
                int col = rs.getInt("COL_IDX");
                double value = rs.getDouble("CELL_VALUE");
                if (row > data.size() - 1) {
                    data.add(row, new ArrayList<>());
                }
                data.get(row).add(col, value);
            }

        } catch (SQLException e) {
            throw new CorruptedPwmException(e);
        }
        return PwmUtils.mapToDoubleMatrix(data, EPSILON);
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
