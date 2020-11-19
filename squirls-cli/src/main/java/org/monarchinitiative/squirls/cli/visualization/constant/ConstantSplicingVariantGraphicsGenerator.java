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

package org.monarchinitiative.squirls.cli.visualization.constant;


import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.cli.visualization.VisualizableVariantAllele;

/**
 * This class returns the same SVG for each variant. Not really useful for anything except for testing.
 */
public class ConstantSplicingVariantGraphicsGenerator implements SplicingVariantGraphicsGenerator {

    /**
     * This is the SVG image that is returned for each variant
     */
    private static final String CONSTANT_SVG_STRING = "<svg width=\"400\" height=\"400\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:svg=\"http://www.w3.org/2000/svg\">\n" +
            "<!-- Created by vmvt -->\n" +
            "<style>\n" +
            "  text { font: 24px monospace; }\n" +
            "  </style>\n" +
            "<g>\n" +
            "<g transform='translate(20,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">-3</text>\n" +
            "</g>\n" +
            "<g transform='translate(35,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">-2</text>\n" +
            "</g>\n" +
            "<g transform='translate(50,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">-1</text>\n" +
            "</g>\n" +
            "<g transform='translate(67,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">1</text>\n" +
            "</g>\n" +
            "<g transform='translate(82,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">2</text>\n" +
            "</g>\n" +
            "<g transform='translate(97,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">3</text>\n" +
            "</g>\n" +
            "<g transform='translate(112,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">4</text>\n" +
            "</g>\n" +
            "<g transform='translate(127,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">5</text>\n" +
            "</g>\n" +
            "<g transform='translate(142,60) scale(0.4,0.4) '>\n" +
            "<text x=\"0\" y=\"0\" fill=\"black\">6</text>\n" +
            "</g>\n" +
            "<line x1=\"65\" y1=\"42\" x2=\"65\" y2=\"70\" stroke=\"red\"/>\n" +
            "<text x=\"20\" y=\"100\" fill=\"#4dbbd5\">a</text>\n" +
            "<text x=\"35\" y=\"100\" fill=\"#4dbbd5\">a</text>\n" +
            "<text x=\"50\" y=\"100\" fill=\"#00A087\">g</text>\n" +
            "<text x=\"65\" y=\"100\" fill=\"#00A087\">g</text>\n" +
            "<text x=\"80\" y=\"100\" fill=\"#ffdf00\">t</text>\n" +
            "<text x=\"95\" y=\"100\" fill=\"#e64b35\">c</text>\n" +
            "<text x=\"110\" y=\"100\" fill=\"#4dbbd5\">a</text>\n" +
            "<text x=\"125\" y=\"100\" fill=\"#00A087\">g</text>\n" +
            "<text x=\"140\" y=\"100\" fill=\"#4dbbd5\">a</text>\n" +
            "<text x=\"65\" y=\"120\" fill=\"#4dbbd5\">a</text>\n" +
            "<rect x=\"65.000000\" y=\"80\" width=\"15\" height=\"49\" rx=\"2\" fill-opacity=\"0.1\" style=\"stroke-width:2; stroke:rgb(4, 12, 4);\"/><g transform='translate(20,180.000000) scale(1,0.042389)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(20,179.256327) scale(1,0.039201)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(20,178.568585) scale(1,0.021962)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(20,178.183284) scale(1,0.014523)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(35,180.000000) scale(1,0.308449)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(35,174.588622) scale(1,0.067201)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(35,173.409655) scale(1,0.056565)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(35,172.417286) scale(1,0.051730)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(50,180.000000) scale(1,0.815783)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(50,165.688013) scale(1,0.098177)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(50,163.965603) scale(1,0.069838)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(50,162.740383) scale(1,0.028340)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(65,180.000000) scale(1,1.948552)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(65,145.814875) scale(1,0.003913)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(65,145.746230) scale(1,0.001956)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(65,145.711908) scale(1,0.001956)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(80,180.000000) scale(1,1.857075)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(80,147.419742) scale(1,0.022601)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(80,147.023228) scale(1,0.001883)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(80,146.990185) scale(1,0.001883)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(95,180.000000) scale(1,0.425298)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(95,172.538631) scale(1,0.241501)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(95,168.301773) scale(1,0.022797)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(95,167.901833) scale(1,0.022084)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(110,180.000000) scale(1,0.412697)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(110,172.759699) scale(1,0.073717)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(110,171.466410) scale(1,0.070092)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(110,170.236725) scale(1,0.047735)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(125,180.000000) scale(1,0.665538)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(125,168.323895) scale(1,0.078552)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(125,166.945782) scale(1,0.068194)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(125,165.749397) scale(1,0.051793)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(140,180.000000) scale(1,0.084801)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">T</text>\n" +
            "</g><g transform='translate(140,178.512271) scale(1,0.033885)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">G</text>\n" +
            "</g><g transform='translate(140,177.917803) scale(1,0.031756)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">A</text>\n" +
            "</g><g transform='translate(140,177.360682) scale(1,0.026966)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">C</text>\n" +
            "</g><g transform='translate(80.000000,231)  scale(1,6.965784)  rotate(180)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\" opacity=\"0.4\" style=\"text-shadow: 2px 2px #FF0000;\">a</text>\n" +
            "</g>\n" +
            "<g transform='translate(20,230) scale(1,0.409255)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">a</text>\n" +
            "</g><g transform='translate(35,230) scale(1,1.351628)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">a</text>\n" +
            "</g><g transform='translate(50,230) scale(1,1.688852)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">g</text>\n" +
            "</g><g transform='translate(65,230) scale(1,1.994218)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">g</text>\n" +
            "</g><g transform='translate(80,230) scale(1,1.979660)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#ffdf00\">t</text>\n" +
            "</g><g transform='translate(110.000000,231)  scale(1,3.011588)  rotate(180)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#e64b35\">c</text>\n" +
            "</g>\n" +
            "<g transform='translate(110,230) scale(1,1.449957)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">a</text>\n" +
            "</g><g transform='translate(125,230) scale(1,1.624803)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#00A087\">g</text>\n" +
            "</g><g transform='translate(155.000000,231)  scale(1,0.481969)  rotate(180)'>\n" +
            "<text x=\"0\" y=\"0\" fill=\"#4dbbd5\">a</text>\n" +
            "</g>\n" +
            "<g fill=\"none\" stroke=\"black\" stroke-width=\"1\">\n" +
            "<path stroke-dasharray=\"2,2\" d=\"M20 230 L150 230\"/>\n" +
            "</g>\n" +
            "</g>\n" +
            "</svg>\n";

    @Override
    public String generateGraphics(VisualizableVariantAllele variant) {
        return CONSTANT_SVG_STRING;
    }
}
