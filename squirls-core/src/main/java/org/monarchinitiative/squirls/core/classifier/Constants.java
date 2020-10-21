package org.monarchinitiative.squirls.core.classifier;

/**
 * Static utility class to hold constants used in the <code>org.monarchinitiative.squirls.core.classifier</code>
 * package.
 */
public class Constants {

    /**
     * The name of the donor site-specific estimator.
     */
    public static final String DONOR_PIPE_NAME = "donor";

    /**
     * The name of the acceptor site-specific estimator.
     */
    public static final String ACCEPTOR_PIPE_NAME = "acceptor";

    /**
     * The name of random forest that is a part of donor site-specific estimator.
     */
    public static final String DONOR_RF_NAME = "donor_rf";

    /**
     * The name of random forest that is a part of acceptor site-specific estimator.
     */
    public static final String ACCEPTOR_RF_NAME = "acceptor_rf";

    private Constants() {
        // private no-op
    }


}
