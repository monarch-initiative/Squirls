package org.monarchinitiative.squirls.io.classifier.v046;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.classifier.SquirlsFeatures;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This is a functional test, we do not check if the classifier was restored into a specific state, but we check if
 * using the same input features yields the expected pathogenicity values. See the parametric test below.
 */
public class SquirlsClassifierDeserializerV046Test {

    private static final Path ENSEMBLE_LR_PATH = Path.of("src/test/resources/org/monarchinitiative/squirls/io/classifier/ensemble.lr.rf.v0.4.6.yaml");

    private static final double TOLERANCE = 5E-10;

    private SquirlsClassifierDeserializerV046 deserializer;
    private SquirlsClassifier clf;

    private static SquirlsFeatures makeFeature046(String payload) {
        double[] features = Arrays.stream(payload.split(",")).mapToDouble(Double::valueOf).toArray();
        return makeFeature046(features[0], features[1], features[2],
                features[3], features[4], features[5],
                features[6], features[7], features[8], features[9],
                features[10], features[11], features[12],
                features[13], features[14]);
    }

    private static SquirlsFeatures makeFeature046(double donorOffset, double canonicalDonor, double crypticDonor,
                                                  double sStrengthDiffDonor, double wtRiDonor, double altRiBestWindowDonor,
                                                  double phylop, double exonLength, double hexamer, double septamer,
                                                  double acceptorOffset, double canonicalAcceptor, double crypticAcceptor,
                                                  double createsYagInAgez, double createsAgInAgez) {
        /*
        The feature payload strings contain the features in this order:
        ['donor_offset', 'canonical_donor', 'cryptic_donor',
        's_strength_diff_donor', 'wt_ri_donor', 'alt_ri_best_window_donor',

        'phylop', 'exon_length', 'hexamer', 'septamer',

        'acceptor_offset', 'canonical_acceptor', 'cryptic_acceptor', 'creates_yag_in_agez', 'creates_ag_in_agez']
        */

        Map<String, Double> general = Map.of("phylop", phylop,
                "exon_length", exonLength,
                "hexamer", hexamer,
                "septamer", septamer);

        Map<String, Double> donorFeatures = Map.of(
                "donor_offset", donorOffset,
                "canonical_donor", canonicalDonor,
                "cryptic_donor", crypticDonor,
                "s_strength_diff_donor", sStrengthDiffDonor,
                "wt_ri_donor", wtRiDonor,
                "alt_ri_best_window_donor", altRiBestWindowDonor);

        Map<String, Double> acceptorFeatures = Map.of("acceptor_offset", acceptorOffset, "canonical_acceptor", canonicalAcceptor,
                "cryptic_acceptor", crypticAcceptor, "creates_yag_in_agez", createsYagInAgez, "creates_ag_in_agez", createsAgInAgez);

        Map<String, Double> features = new HashMap<>(general);
        features.putAll(donorFeatures);
        features.putAll(acceptorFeatures);
        return SquirlsFeatures.of(features);
    }

    @BeforeEach
    public void setUp() throws Exception {
        deserializer = new SquirlsClassifierDeserializerV046();
        try (InputStream is = Files.newInputStream(ENSEMBLE_LR_PATH)) {
            clf = deserializer.deserialize(is);
        }
    }

    @Test
    public void supportedVersions() {
        assertThat(deserializer.supportedVersions(), hasSize(1));
        assertThat(deserializer.supportedVersions(), hasItem(SquirlsClassifierVersion.v0_4_6));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    // chr10-14977461-C-T (donor canonical)
                    ".953819555  | true  | 1,8.960001932068081,1.3581999408672178,-14.235159339682536,5.079853328257856,-2.5219486629430072,7.320000171661378,102.0,1.6480093999999996,1.3925,103,0.0,-12.347213724695143,0.0,0.0",
                    // chr10-79769277-C-T (donor cryptic)
                    ".065053552  | true  | 18,0.0,2.313614788441317,0.0,5.863555772811793,8.17717056125311,-0.1289999932050705,-1.0,-1.9681822,-1.8941,157,0.0,-10.441640936320768,0.0,0.0",
                    // chr17-44087661-T-C (canonical acceptor disrupted)
                    ".007029674  | false  | -108,0.0,-17.690125476291747,0.0,5.879652903517925,-11.810472572773822,0.9229999780654908,93.0,0.862883,1.4614,-15,0.7297598244333532,0.0,0.0,0.0",
                    // chr1-100381954-A-G (AG in AGEZ, likely cryptic site or skipping)
                    ".999509533  | true  | -100,0.0,-7.191083224974222,0.0,5.262950400223763,-1.9281328247504608,0.22499999403953552,88.0,-0.6826994000000001,-0.7658,-12,-0.1415541442140178,0.08920402336736544,1.0,1.0",
                    // chr1-10032249-A-G (+3 variant)
                    ".227995695  | true  | 3,0.8164456581284618,0.0,1.2388665578044202,8.90000316861008,8.083557510481619,2.440000057220459,171.0,2.00755591,1.9646,174,0.0,-25.95792510598073,0.0,0.0",
                    // chr1-100327079-T-C (coding, close to acceptor site)
                    ".005705941  | false | -191,0.0,-17.039868241197162,2.0503957850253407,10.242210133396183,-6.797658107800977,0.8889999985694885,211.0,1.5518906,1.0495,21,0.0,-6.0547923606482605,0.0,0.0"
            })
    public void deserialize(double expectedPathogenicity, boolean isPathogenic, String payload) throws Exception {
        Prediction prediction = clf.predict(makeFeature046(payload));
        assertThat(prediction.getMaxPathogenicity(), closeTo(expectedPathogenicity, TOLERANCE));
        assertThat(prediction.isPositive(), equalTo(isPathogenic));
    }
}