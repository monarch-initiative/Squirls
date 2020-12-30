/*
 * SOFTWARE LICENSE AGREEMENT
 * FOR NON-COMMERCIAL USE
 * 	This Software License Agreement (this “Agreement”) is made between you (“You,” “Your,” or “Licensee”) and The
 * 	Jackson Laboratory (“Licensor”). This Agreement grants to You a license to the Licensed Software subject to Your
 * 	acceptance of all the terms and conditions contained in this Agreement. Please read the terms and conditions
 * 	carefully. You accept the terms and conditions set forth herein by using, downloading or opening the software
 *
 * 1. LICENSE
 *
 * 1.1	Grant. Subject to the terms and conditions of this Agreement, Licensor hereby grants to Licensee a worldwide,
 * royalty-free, non-exclusive, non-transferable, non-sublicensable license to download, copy, display, and use the
 * Licensed Software for Non-Commercial purposes only. “Licensed Software” means the current version of the software.
 * “Non-Commercial” means not intended or directed toward commercial advantage or monetary compensation.
 *
 * 1.2	License Limitations. Nothing in this Agreement shall be construed to confer any rights upon Licensee except as
 * expressly granted herein. Licensee may not use or exploit the Licensed Software other than expressly permitted by this
 * Agreement. Licensee may not, nor may Licensee permit any third party, to modify, translate, reverse engineer, decompile,
 * disassemble or create derivative works based on the Licensed Software or any portion thereof. Subject to Section 1.1,
 * Licensee may distribute the Licensed Software to a third party, provided that the recipient agrees to use the Licensed
 * Software on the terms and conditions of this Agreement. Licensee acknowledges that Licensor reserves the right to offer
 * to Licensee or any third party a license for commercial use and distribution of the Licensed Software on terms and
 * conditions different than those contained in this Agreement.
 *
 * 2. OWNERSHIP OF INTELLECTUAL PROPERTY
 *
 * 2.1	Ownership Rights. Except for the limited license rights expressly granted to Licensee under this Agreement, Licensee
 * acknowledges that all right, title and interest in and to the Licensed Software and all intellectual property rights
 * therein shall remain with Licensor or its licensors, as applicable.
 *
 * 3. DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY
 *
 * 3.1 	Disclaimer of Warranty. LICENSOR PROVIDES THE LICENSED SOFTWARE ON A NO-FEE BASIS “AS IS” WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED. LICENSOR EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS OF ANY KIND, INCLUDING ANY
 * WARRANTY OF MERCHANTABILITY, TITLE, SECURITY, ACCURACY, NON-INFRINGEMENT OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 3,2	Limitation of Liability.  LICENSEE ASSUMES FULL RESPONSIBILITY AND RISK FOR ANY LOSS RESULTING FROM LICENSEE’s
 * DOWNLOADING AND USE OF THE LICENSED SOFTWARE.  IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, ARISING FROM THE LICENSED SOFTWARE OR LICENSEE’S USE OF
 * THE LICENSED SOFTWARE, REGARDLESS OF WHETHER LICENSOR IS ADVISED, OR HAS OTHER REASON TO KNOW, OR IN FACT KNOWS,
 * OF THE POSSIBILITY OF THE FOREGOING.
 *
 * 3.3	Acknowledgement. Without limiting the generality of Section 3.1, Licensee acknowledges that the Licensed Software
 * is provided as an information resource only, and should not be relied on for any diagnostic or treatment purposes.
 *
 * 4. TERM AND TERMINATION
 *
 * 4.1 	Term. This Agreement commences on the date this Agreement is executed and will continue until terminated in
 * accordance with Section 4.2.
 *
 * 4.2	Termination. If Licensee breaches any provision hereunder, or otherwise engages in any unauthorized use of the
 * Licensed Software, Licensor may terminate this Agreement immediately. Licensee may terminate this Agreement at any
 * time upon written notice to Licensor. Upon termination, the license granted hereunder will terminate and Licensee will
 * immediately cease using the Licensed Software and destroy all copies of the Licensed Software in its possession.
 * Licensee will certify in writing that it has complied with the foregoing obligation.
 *
 * 5. MISCELLANEOUS
 *
 * 5.1	Future Updates. Use of the Licensed Software under this Agreement is subject to the terms and conditions contained
 * herein. New or updated software may require additional or revised terms of use. Licensor will provide notice of and
 * make available to Licensee any such revised terms.
 *
 * 5.2	Entire Agreement. This Agreement, including any Attachments hereto, constitutes the sole and entire agreement
 * between the parties as to the subject matter set forth herein and supersedes are previous license agreements,
 * understandings, or arrangements between the parties relating to such subject matter.
 *
 * 5.2 	Governing Law. This Agreement shall be construed, governed, interpreted and applied in accordance with the
 * internal laws of the State of Maine, U.S.A., without regard to conflict of laws principles. The parties agree that
 * any disputes between them may be heard only in the state or federal courts in the State of Maine, and the parties
 * hereby consent to venue and jurisdiction in those courts.
 *
 * version:6-8-18
 *
 * Daniel Danis, Peter N Robinson, 2020
 */

package org.monarchinitiative.squirls.io.classifier.v041;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.squirls.core.Prediction;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.io.SimpleSquirlsFeatures;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SquirlsClassifierDeserializerV041Test {

    private static final Path TOY_MODEL_v0_4_1 = Paths.get("src/test/resources/example_model.v0.4.1.yaml");
    private static final double TOLERANCE = 5E-10;

    private SquirlsClassifierDeserializerV041 deserializerV041;

    private SquirlsClassifier clf;

    private static SimpleSquirlsFeatures makeFeature041(String payload) {
        double[] features = Arrays.stream(payload.split(",")).mapToDouble(Double::valueOf).toArray();
        return makeFeature041(features[0], features[1], features[2],
                features[3], features[4], features[5],
                features[6], features[7], features[8], features[9],
                features[10], features[11], features[12],
                features[13], features[14]);
    }

    private static SimpleSquirlsFeatures makeFeature041(double donorOffset, double canonicalDonor, double crypticDonor,
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

        Map<String, Object> donorFeatures = Map.of(
                "donor_offset", donorOffset,
                "canonical_donor", canonicalDonor,
                "cryptic_donor", crypticDonor,
                "s_strength_diff_donor", sStrengthDiffDonor,
                "wt_ri_donor", wtRiDonor,
                "alt_ri_best_window_donor", altRiBestWindowDonor);

        Map<String, Object> acceptorFeatures = Map.of("acceptor_offset", acceptorOffset, "canonical_acceptor", canonicalAcceptor,
                "cryptic_acceptor", crypticAcceptor, "creates_yag_in_agez", createsYagInAgez, "creates_ag_in_agez", createsAgInAgez);

        Map<String, Object> features = new HashMap<>(general);
        features.putAll(donorFeatures);
        features.putAll(acceptorFeatures);
        return new SimpleSquirlsFeatures(features);
    }

    @BeforeEach
    public void setUp() throws Exception {
        deserializerV041 = new SquirlsClassifierDeserializerV041();
        try (InputStream is = Files.newInputStream(TOY_MODEL_v0_4_1)) {
            clf = deserializerV041.deserialize(is);
        }
    }

    @Test
    public void supportedVersions() {
        assertThat(deserializerV041.supportedVersions(), hasSize(1));
        assertThat(deserializerV041.supportedVersions(), hasItem(SquirlsClassifierVersion.v0_4_1));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value =
            {
                    // chr10-14977461-C-T (donor canonical)
                    ".948839494  | true  | 1,8.960001932068081,1.3581999408672178,-14.235159339682536,5.079853328257856,-2.5219486629430072,7.320000171661378,102.0,1.6480093999999996,1.3925,103,0.0,-12.347213724695143,0.0,0.0",
                    // chr10-79769277-C-T (donor cryptic)
                    ".068427743  | true  | 18,0.0,2.313614788441317,0.0,5.863555772811793,8.17717056125311,-0.1289999932050705,-1.0,-1.9681822,-1.8941,157,0.0,-10.441640936320768,0.0,0.0",
                    // chr17-44087661-T-C (canonical acceptor disrupted)
                    ".007992607  | false  | -108,0.0,-17.690125476291747,0.0,5.879652903517925,-11.810472572773822,0.9229999780654908,93.0,0.862883,1.4614,-15,0.7297598244333532,0.0,0.0,0.0",
                    // chr1-100381954-A-G (AG in AGEZ, likely cryptic site or skipping)
                    ".999077965  | true  | -100,0.0,-7.191083224974222,0.0,5.262950400223763,-1.9281328247504608,0.22499999403953552,88.0,-0.6826994000000001,-0.7658,-12,-0.1415541442140178,0.08920402336736544,1.0,1.0",
                    // chr1-10032249-A-G (+3 variant)
                    ".231940480  | true  | 3,0.8164456581284618,0.0,1.2388665578044202,8.90000316861008,8.083557510481619,2.440000057220459,171.0,2.00755591,1.9646,174,0.0,-25.95792510598073,0.0,0.0",
                    // chr1-100327079-T-C (coding, close to acceptor site)
                    ".006574573  | false | -191,0.0,-17.039868241197162,2.0503957850253407,10.242210133396183,-6.797658107800977,0.8889999985694885,211.0,1.5518906,1.0495,21,0.0,-6.0547923606482605,0.0,0.0"
            })
    public void deserializeOverallModelData_v041(double expectedPathogenicity, boolean isPathogenic, String payload) throws Exception {
        Prediction prediction = clf.predict(makeFeature041(payload));
        assertThat(prediction.getMaxPathogenicity(), closeTo(expectedPathogenicity, TOLERANCE));
        assertThat(prediction.isPositive(), equalTo(isPathogenic));
    }
}