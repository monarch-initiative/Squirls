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

package org.monarchinitiative.squirls.core.classifier.io;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.squirls.core.TestDataSourceConfig;
import org.monarchinitiative.squirls.core.classifier.TestBasedOnIrisInstances;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;

class DeserializerTest extends TestBasedOnIrisInstances {

    private static final Path TOY_MODEL_v0_4_1 = Paths.get(DeserializerTest.class.getResource("example_model.v0.4.1.yaml").getPath());

    static List<List<Integer>> parseExpected(String expected) {
        final Pattern group = Pattern.compile("\\[(?<left>\\d+),\\s*(?<right>\\d+)\\]");
        final Matcher matcher = group.matcher(expected);

        List<List<Integer>> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(List.of(Integer.parseInt(matcher.group("left")), Integer.parseInt(matcher.group("right"))));
        }
        return list;
    }

    @Test
    void deserializeModel_v1_1() throws Exception {
        final OverallModelData data;
        try (InputStream is = Files.newInputStream(TestDataSourceConfig.SQUIRLS_MODEL_PATH)) {
            data = Deserializer.deserializeOverallModelData(is);
        }
        assertThat(data, is(notNullValue()));

        assertThat(data.getDonorThreshold(), is(closeTo(.012354456, 1e-5)));
        assertThat(data.getAcceptorThreshold(), is(closeTo(.009894591, 1e-5)));

        final PipelineTransferModel donorPipe = data.getDonorClf();
        final RandomForestTransferModel donorClf = donorPipe.getRf();
        assertThat(donorClf.getClasses(), contains(0, 1));

        final Map<Integer, DecisionTreeTransferModel> donorTrees = donorClf.getTrees();
        assertThat(donorTrees.keySet(), hasItems(0, 1));
        String expectedValues = "[[33637, 1280], [31736, 191], [31191, 143], [26828, 84], [496, 10], [445, 6], [17, 2], [428, 4], [51, 4], [26332, 74], [2345, 16], [2334, 14], [11, 2], [23987, 58], [13977, 17], [10010, 41], [4363, 59], [4258, 41], [4165, 35], [0, 1], [4165, 34], [93, 6], [11, 5], [82, 1], [105, 18], [0, 4], [105, 14], [0, 2], [105, 12], [545, 48], [76, 31], [60, 12], [16, 19], [469, 17], [468, 12], [377, 3], [351, 2], [26, 1], [91, 9], [0, 4], [91, 5], [1, 5], [1901, 1089], [1662, 317], [1335, 136], [1145, 61], [1145, 59], [72, 15], [1073, 44], [0, 2], [190, 75], [190, 66], [19, 36], [171, 30], [0, 9], [327, 181], [326, 162], [78, 83], [25, 54], [53, 29], [248, 79], [189, 26], [59, 53], [1, 19], [239, 772], [139, 344], [129, 344], [75, 74], [3, 22], [72, 52], [54, 270], [6, 5], [48, 265], [10, 0], [100, 428], [32, 13], [68, 415], [44, 383], [41, 383], [3, 0], [24, 32]]";
        List<List<Integer>> expVals = null;
        assertThat(donorTrees.get(0), is(DecisionTreeTransferModel.builder()
                .childrenLeft(List.of(1, 2, 3, 4, 5, 6, -1, -1, -1, 10, 11, -1, -1, 14, -1, -1, 17, 18, 19, -1, -1, 22, -1, -1, 25, -1, 27, -1, -1, 30, 31, -1, -1, 34, 35, 36, -1, -1, 39, -1, -1, -1, 43, 44, 45, 46, 47, -1, -1, -1, 51, 52, -1, -1, -1, 56, 57, 58, -1, -1, 61, -1, -1, -1, 65, 66, 67, 68, -1, -1, 71, -1, -1, -1, 75, -1, 77, 78, -1, -1, -1))
                .childrenRight(List.of(42, 29, 16, 9, 8, 7, -1, -1, -1, 13, 12, -1, -1, 15, -1, -1, 24, 21, 20, -1, -1, 23, -1, -1, 26, -1, 28, -1, -1, 33, 32, -1, -1, 41, 38, 37, -1, -1, 40, -1, -1, -1, 64, 55, 50, 49, 48, -1, -1, -1, 54, 53, -1, -1, -1, 63, 60, 59, -1, -1, 62, -1, -1, -1, 74, 73, 70, 69, -1, -1, 72, -1, -1, -1, 76, -1, 80, 79, -1, -1, -1))
                .feature(List.of(2, 3, 3, 5, 3, 4, -2, -2, -2, 5, 4, -2, -2, 0, -2, -2, 2, 4, 5, -2, -2, 0, -2, -2, 3, -2, 2, -2, -2, 5, 0, -2, -2, 2, 0, 2, -2, -2, 3, -2, -2, -2, 1, 1, 2, 5, 0, -2, -2, -2, 3, 5, -2, -2, -2, 4, 1, 5, -2, -2, 3, -2, -2, -2, 5, 5, 3, 5, -2, -2, 4, -2, -2, -2, 1, -2, 0, 1, -2, -2, -2))
                .nodeCount(81)
                .threshold(List.of(-0.0023636077530682087, 7.2795000076293945, 1.834500014781952, -2.087249994277954, 1.0009838342666626, -4.227508306503296, -2.0, -2.0, -2.0, -1.2717499732971191, 0.5419792532920837, -2.0, -2.0, -5.5, -2.0, -2.0, -2.685951352119446, 2.4630032777786255, -4.3678001165390015, -2.0, -2.0, -70.5, -2.0, -2.0, 1.8740000128746033, -2.0, -2.649991035461426, -2.0, -2.0, -1.1361500024795532, -17.5, -2.0, -2.0, -1.644979178905487, -15.5, -4.9203314781188965, -2.0, -2.0, 7.36650013923645, -2.0, -2.0, -2.0, 2.466026186943054, 0.7217011153697968, 1.127469539642334, 3.2206499576568604, -3.5, -2.0, -2.0, -2.0, 8.967999935150146, -1.567900002002716, -2.0, -2.0, -2.0, 2.648341417312622, 0.8261631429195404, 1.2654500007629395, -2.0, -2.0, 3.0119999647140503, -2.0, -2.0, -2.0, 0.37870000302791595, 0.365449994802475, 1.5750000476837158, -1.3238999843597412, -2.0, -2.0, -2.516780972480774, -2.0, -2.0, -2.0, 2.815334439277649, -2.0, 3.5, 5.207538843154907, -2.0, -2.0, -2.0))
                //
                .values(parseExpected(expectedValues))
                .build()));

        // in addition to all other attributes of the v model, `v1.1` also has `intercept` and `slope` fields
        assertThat(data.getIntercept().get(0), is(closeTo(-4.909676356421783, EPSILON)));
        assertThat(data.getSlope().get(0).get(0), is(closeTo(13.648421772211595, EPSILON)));
    }

    @Test
    public void deserializeOverallModelData_v041() throws Exception {
        final OverallModelData data;
        try (InputStream is = Files.newInputStream(TOY_MODEL_v0_4_1)) {
            data = Deserializer.deserializeOverallModelData(is);
        }
        assertThat(data, is(notNullValue()));
    }
}