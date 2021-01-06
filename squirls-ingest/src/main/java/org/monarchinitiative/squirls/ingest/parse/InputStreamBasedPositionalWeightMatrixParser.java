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

package org.monarchinitiative.squirls.ingest.parse;

import org.monarchinitiative.squirls.core.reference.DoubleMatrix;
import org.monarchinitiative.squirls.core.reference.SplicingParameters;
import org.monarchinitiative.squirls.core.reference.SplicingPwmData;
import org.monarchinitiative.squirls.io.SplicingPositionalWeightMatrixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Decode Yaml file with <em>position weight matrix</em> definitions into {@link PositionWeightMatrix} objects.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @see PositionWeightMatrix
 */
public class InputStreamBasedPositionalWeightMatrixParser implements SplicingPositionalWeightMatrixParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamBasedPositionalWeightMatrixParser.class);

    private static final String DONOR_M_NAME = "SPLICE_DONOR_SITE";

    private static final String ACCEPTOR_M_NAME = "SPLICE_ACCEPTOR_SITE";

    private final DoubleMatrix donorMatrix;

    private final DoubleMatrix acceptorMatrix;

    private final SplicingParameters splicingParameters;

    /**
     * Parse Yaml content of provided input stream, create PWM representations ({@link PositionWeightMatrix}) and store
     * in a {@link Map} by their names.
     *
     * @param is {@link InputStream} with PWM definitions in Yaml format as described in {@link PositionWeightMatrix} class
     *           description
     */
    public InputStreamBasedPositionalWeightMatrixParser(InputStream is) {
        Map<String, PositionWeightMatrix> matrixMap = parseAll(is);
        this.donorMatrix = SplicingPositionalWeightMatrixParser.mapToDoubleMatrix(matrixMap.get(DONOR_M_NAME).getMatrix(), EPSILON);
        this.acceptorMatrix = SplicingPositionalWeightMatrixParser.mapToDoubleMatrix(matrixMap.get(ACCEPTOR_M_NAME).getMatrix(), EPSILON);
        this.splicingParameters = SplicingParameters.of(
                matrixMap.get(DONOR_M_NAME).getExon(), matrixMap.get(DONOR_M_NAME).getIntron(),
                matrixMap.get(ACCEPTOR_M_NAME).getExon(), matrixMap.get(ACCEPTOR_M_NAME).getIntron());
    }


    /**
     * Decode records from provided Yaml file into corresponding {@link PositionWeightMatrix} objects, store them in a {@link Map}
     * by their names.
     *
     * @param is {@link InputStream} with PWM definitions in Yaml format as described in {@link PositionWeightMatrix} class
     *           description
     * @return {@link Map} key - PWM name, Value - {@link PositionWeightMatrix} object.
     */
    private static Map<String, PositionWeightMatrix> parseAll(InputStream is) {
        Map<String, PositionWeightMatrix> matrixMap = new HashMap<>();

        Yaml yaml = new Yaml(new Constructor(PositionWeightMatrix.class));
        for (Object object : yaml.loadAll(is)) {
            PositionWeightMatrix matrix = (PositionWeightMatrix) object;

            String name = matrix.getName();
            matrixMap.put(name, matrix);
        }
        return matrixMap;
    }

    @Override
    public SplicingPwmData getSplicingPwmData() {
        return SplicingPwmData.builder()
                .setDonor(donorMatrix)
                .setAcceptor(acceptorMatrix)
                .setParameters(splicingParameters)
                .build();
    }

    /**
     * This POJO represents a position-weight matrix (PWM). The PWM attributes are:
     * <ul>
     * <li><b>name</b> - name of the PWM</li>
     * <li><b>matrix</b> - internal representation of PWM values used for scoring of nucleotide sequences</li>
     * </ul>
     */
    public static class PositionWeightMatrix {

        private String name;

        private List<List<Double>> matrix;

        private int exon;

        private int intron;


        public PositionWeightMatrix() {
        }

        public int getExon() {
            return exon;
        }

        public void setExon(int exon) {
            this.exon = exon;
        }

        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public List<List<Double>> getMatrix() {
            return matrix;
        }


        public void setMatrix(List<List<Double>> matrix) {
            this.matrix = matrix;
        }

        public int getIntron() {
            return intron;
        }

        public void setIntron(int intron) {
            this.intron = intron;
        }
    }
}
