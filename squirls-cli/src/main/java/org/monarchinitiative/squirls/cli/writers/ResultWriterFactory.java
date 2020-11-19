package org.monarchinitiative.squirls.cli.writers;

import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.cli.writers.html.HtmlResultWriter;
import org.monarchinitiative.squirls.cli.writers.vcf.VcfResultWriter;

public class ResultWriterFactory {

    private final SplicingVariantGraphicsGenerator graphicsGenerator;

    public ResultWriterFactory(SplicingVariantGraphicsGenerator graphicsGenerator) {
        this.graphicsGenerator = graphicsGenerator;
    }

    public ResultWriter resultWriterForFormat(OutputFormat outputFormat) {
        switch (outputFormat) {
            case HTML:
                return new HtmlResultWriter(graphicsGenerator);
            case VCF:
                return new VcfResultWriter();
            default:
                // should not happen
                throw new RuntimeException("Unknown output format `" + outputFormat + "`");
        }
    }

}
