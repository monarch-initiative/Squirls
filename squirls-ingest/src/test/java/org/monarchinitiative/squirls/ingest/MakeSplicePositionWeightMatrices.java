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

package org.monarchinitiative.squirls.ingest;

import org.jblas.DoubleMatrix;
import org.monarchinitiative.squirls.core.reference.SplicingParameters;


public class MakeSplicePositionWeightMatrices {

    private MakeSplicePositionWeightMatrices() {
    }


    // This matrix is expected to be read from the test YAML file at "/genome/dao/splicing/spliceSites.yaml"
    public static DoubleMatrix makeDonorMatrix() {
        return new DoubleMatrix(new double[][]{
                // -3                  +1 (G) +2 (U)                +5     +6
                {0.332, 0.638, 0.097, 0.002, 0.001, 0.597, 0.683, 0.091, 0.179}, // A
                {0.359, 0.107, 0.028, 0.001, 0.012, 0.031, 0.079, 0.060, 0.152}, // C
                {0.186, 0.117, 0.806, 0.996, 0.001, 0.339, 0.122, 0.771, 0.191}, // G
                {0.123, 0.139, 0.069, 0.001, 0.986, 0.032, 0.116, 0.079, 0.478}  // T
        });
    }


    public static DoubleMatrix makeAcceptorMatrix() {
        return new DoubleMatrix(new double[][]{
                {0.248, 0.242, 0.238, 0.228, 0.215, 0.201, 0.181, 0.164, 0.147, 0.136, 0.125, 0.116, 0.106, 0.097, 0.090, 0.091, 0.102, 0.111, 0.117, 0.092, 0.096, 0.240, 0.063, 0.997, 0.001, 0.261, 0.251}, // A
                {0.242, 0.247, 0.250, 0.250, 0.253, 0.255, 0.263, 0.268, 0.271, 0.274, 0.281, 0.278, 0.274, 0.278, 0.259, 0.281, 0.292, 0.319, 0.330, 0.339, 0.293, 0.272, 0.644, 0.001, 0.002, 0.147, 0.191}, // C
                {0.159, 0.155, 0.154, 0.151, 0.151, 0.148, 0.148, 0.142, 0.138, 0.136, 0.128, 0.122, 0.116, 0.107, 0.102, 0.108, 0.114, 0.104, 0.093, 0.066, 0.066, 0.207, 0.006, 0.001, 0.996, 0.478, 0.194}, // G
                {0.350, 0.355, 0.358, 0.371, 0.381, 0.397, 0.409, 0.426, 0.444, 0.454, 0.466, 0.483, 0.504, 0.518, 0.549, 0.519, 0.491, 0.465, 0.460, 0.504, 0.545, 0.281, 0.287, 0.001, 0.001, 0.114, 0.365}  // T
        });
    }

    public static SplicingParameters makeSplicingParameters() {
        return SplicingParameters.builder()
                .setDonorExonic(3)
                .setDonorIntronic(6)
                .setAcceptorExonic(2)
                .setAcceptorIntronic(25)
                .build();
    }
}
