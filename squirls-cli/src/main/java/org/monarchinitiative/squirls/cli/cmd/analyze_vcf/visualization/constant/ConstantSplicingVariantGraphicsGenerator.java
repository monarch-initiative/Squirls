package org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.constant;

import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.SplicingVariantAlleleEvaluation;
import org.monarchinitiative.squirls.cli.cmd.analyze_vcf.visualization.SplicingVariantGraphicsGenerator;

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
    public SplicingVariantAlleleEvaluation generateGraphics(SplicingVariantAlleleEvaluation variant) {
        variant.setPrimaryGraphics(CONSTANT_SVG_STRING);
        return variant;
    }
}
