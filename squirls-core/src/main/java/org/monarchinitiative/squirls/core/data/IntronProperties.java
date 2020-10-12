package org.monarchinitiative.squirls.core.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for parsing intron data from database entry. The properties are stored in database in form `key1=value1;key2=value2`.
 */
class IntronProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntronProperties.class);
    private final double donorScore;
    private final double acceptorScore;

    private IntronProperties(double donorScore, double acceptorScore) {
        this.donorScore = donorScore;
        this.acceptorScore = acceptorScore;
    }

    static IntronProperties parseString(String payload) {
        final String[] tokens = payload.split(";");
        double donor = Double.NaN, acceptor = Double.NaN;
        for (String token : tokens) {
            final String[] subtokens = token.split("=");
            switch (subtokens[0]) {
                case "DONOR":
                    donor = Double.parseDouble(subtokens[1]);
                    break;
                case "ACCEPTOR":
                    acceptor = Double.parseDouble(subtokens[1]);
                    break;
                default:
                    LOGGER.warn("Unknown token `{}` in intron properties `{}`", subtokens[0], payload);
            }
        }
        return new IntronProperties(donor, acceptor);
    }

    double getDonorScore() {
        return donorScore;
    }

    double getAcceptorScore() {
        return acceptorScore;
    }
}
