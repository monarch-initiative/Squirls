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

package org.monarchinitiative.squirls.core.classifier;

import java.util.List;
import java.util.Map;

/**
 * This class contains Python's Scikit-Learn DecisionTreeClassifier trees that were trained on IRIS dataset, as
 * described in individual Javadoc's descriptions.
 */
public class TestTreeInstances {

    private static final Map<Integer, String> IRIS_FEATURE_IDXs = Map.of(
            0, "sepal_length",
            1, "sepal_width",
            2, "petal_length",
            3, "petal_width");

    /**
     * <pre>
     * from sklearn.tree import DecisionTreeClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * # only use the `versicolor` and `virginica` classes
     * Xbin = X[50:, :]
     * ybin = y[50:]
     * dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(Xbin,ybin)
     * </pre>
     *
     * @return tree
     */
    public static BinaryDecisionTree<SquirlsFeatures> getTreeOne() {
        // dtc = DecisionTreeClassifier(random_state=50, max_depth=3).fit(Xbin, ybin)
        return BinaryDecisionTree.builder()
                .name("tree_0")
                .putAllFeatureIndices(IRIS_FEATURE_IDXs)
                .nNodes(13)
                .classes(List.of(1, 2))
                .childrenLeft(List.of(1, 2, 3, -1, -1, 6, -1, -1, 9, 10, -1, -1, -1))
                .childrenRight(List.of(8, 5, 4, -1, -1, 7, -1, -1, 12, 11, -1, -1, -1))
                .thresholds(List.of(1.75, 4.95000005, 1.65000004, -2., -2., 1.58000001, -2., -2., 4.85000014, 3.10000002, -2., -2., -2.))
                .features(List.of(3, 2, 3, -2, -2, 3, -2, -2, 2, 1, -2, -2, -2))
                .values(List.of(
                        List.of(50, 50),
                        List.of(49, 5),
                        List.of(47, 1),
                        List.of(47, 0),
                        List.of(0, 1),
                        List.of(2, 4),
                        List.of(0, 3),
                        List.of(2, 1),
                        List.of(1, 45),
                        List.of(1, 2),
                        List.of(0, 2),
                        List.of(1, 0),
                        List.of(0, 43)))
                .build();
    }

    /**
     * This is the tree <code>one</code> from the following code:
     * <pre>
     * from sklearn.ensemble import RandomForestClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * # only use the `versicolor` and `virginica` classes
     * Xbin = X[:100, :]
     * ybin = y[:100]
     * rfc = RandomForestClassifier(n_estimators=2, max_depth=2, random_state=10).fit(Xbin, ybin)
     * one, two = rfc.estimators_
     * </pre>
     *
     * @return tree <code>one</code>
     */
    public static BinaryDecisionTree<SquirlsFeatures> getRandomForestTreeOne() {
        return BinaryDecisionTree.builder()
                .name("rf_tree_0")
                .putAllFeatureIndices(IRIS_FEATURE_IDXs)
                .nNodes(7)
                .classes(List.of(1, 2))
                .childrenLeft(List.of(1, 2, -1, -1, 5, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, 6, -1, -1))
                .thresholds(List.of(1.55000001, 4.95000005, -2., -2., 5.04999995, -2., -2.))
                .features(List.of(3, 2, -2, -2, 2, -2, -2))
                .values(List.of(
                        List.of(50, 50),
                        List.of(49, 3),
                        List.of(49, 0),
                        List.of(0, 3),
                        List.of(1, 47),
                        List.of(1, 10),
                        List.of(0, 37)))
                .build();
    }

    /**
     * This is the tree <code>two</code> from the following code:
     * <pre>
     * from sklearn.ensemble import RandomForestClassifier
     * from sklearn.datasets import load_iris
     *
     * X, y = load_iris(return_X_y=True)
     * # only use the `versicolor` and `virginica` classes
     * Xbin = X[:100, :]
     * ybin = y[:100]
     * rfc = RandomForestClassifier(n_estimators=2, max_depth=2, random_state=10).fit(Xbin, ybin)
     * one, two = rfc.estimators_
     * </pre>
     *
     * @return tree <code>two</code>
     */
    public static BinaryDecisionTree<SquirlsFeatures> getRandomForestTreeTwo() {
        return BinaryDecisionTree.builder()
                .name("rf_tree_1")
                .putAllFeatureIndices(IRIS_FEATURE_IDXs)
                .nNodes(7)
                .classes(List.of(1, 2))
                .childrenLeft(List.of(1, 2, -1, -1, 5, -1, -1))
                .childrenRight(List.of(4, 3, -1, -1, 6, -1, -1))
                .thresholds(List.of(1.69999999, 5.45000005, -2., -2., 1.84999996, -2., -2.))
                .features(List.of(3, 2, -2, -2, 3, -2, -2))
                .values(List.of(
                        List.of(52, 48),
                        List.of(50, 1),
                        List.of(50, 0),
                        List.of(0, 1),
                        List.of(2, 47),
                        List.of(2, 11),
                        List.of(0, 36)))
                .build();
    }
}
