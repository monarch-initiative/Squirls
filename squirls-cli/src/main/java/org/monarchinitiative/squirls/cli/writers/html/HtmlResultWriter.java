package org.monarchinitiative.squirls.cli.writers.html;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.squirls.cli.visualization.SplicingVariantGraphicsGenerator;
import org.monarchinitiative.squirls.cli.visualization.VisualizableVariantAllele;
import org.monarchinitiative.squirls.cli.writers.*;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Write the {@link AnalysisResults} in HTML format.
 */
public class HtmlResultWriter implements ResultWriter {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    private final TemplateEngine templateEngine;

    private final SplicingVariantGraphicsGenerator generator;

    public HtmlResultWriter(SplicingVariantGraphicsGenerator generator) {
        this.generator = generator;
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    private static VisualizableVariantAllele toVisualizableAllele(WritableSplicingAllele writableSplicingAllele) {
        return new SimpleVisualizableAllele(writableSplicingAllele.variantAnnotations(), writableSplicingAllele.squirlsPredictions());
    }

    private static String getRepresentation(WritableSplicingAllele writableSplicingAllele) {
        VariantContext base = writableSplicingAllele.variantContext();
        Allele allele = writableSplicingAllele.allele();
        return String.format("%s:%s %s>%s",
                base.getContig(),
                NUMBER_FORMAT.format(base.getStart()),
                base.getReference().getBaseString(),
                allele.getBaseString());
    }

    @Override
    public void write(AnalysisResults results, OutputSettings outputSettings) throws IOException {
        Path outputPath = Paths.get(outputSettings.outputPrefix() + '.' + OutputFormat.HTML.getFileExtension());

        // sort results by max squirls pathogenicity and select at most n variants
        List<? extends WritableSplicingAllele> allelesToReport = results.getVariants().stream()
                .sorted(Comparator.comparing(WritableSplicingAllele::maxSquirlsScore).reversed())
                .limit(outputSettings.nVariantsToReport())
                .collect(Collectors.toList());

        List<PresentableVariant> variants = new ArrayList<>();
        for (WritableSplicingAllele sve : allelesToReport) {
            String representation = getRepresentation(sve);
            String graphics = generator.generateGraphics(toVisualizableAllele(sve));

            PresentableVariant presentableVariant = PresentableVariant.of(representation,
                    sve.variantAnnotations().getHighestImpactAnnotation().getGeneSymbol(),
                    sve.maxSquirlsScore(),
                    graphics);

            variants.add(presentableVariant);
        }

        try (Writer writer = Files.newBufferedWriter(outputPath)) {
            Context context = new Context();
            context.setVariable("sampleName", String.join(", ", results.getSampleNames()));
            context.setVariable("settings", results.getSettingsData());
            context.setVariable("stats", results.getAnalysisStats());
            context.setVariable("variants", variants);

            templateEngine.process("results", context, writer);
        }
    }

    private static class SimpleVisualizableAllele implements VisualizableVariantAllele {

        private final VariantAnnotations annotations;

        private final Map<String, SplicingPredictionData> squirlsPredictions;

        private SimpleVisualizableAllele(VariantAnnotations annotations, Map<String, SplicingPredictionData> squirlsPredictions) {
            this.annotations = annotations;
            this.squirlsPredictions = squirlsPredictions;
        }

        @Override
        public VariantAnnotations variantAnnotations() {
            return annotations;
        }

        @Override
        public Map<String, SplicingPredictionData> squirlsPredictions() {
            return squirlsPredictions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleVisualizableAllele that = (SimpleVisualizableAllele) o;
            return Objects.equals(annotations, that.annotations) &&
                    Objects.equals(squirlsPredictions, that.squirlsPredictions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(annotations, squirlsPredictions);
        }

        @Override
        public String toString() {
            return "SimpleVisualizableAllele{" +
                    "annotations=" + annotations +
                    ", squirlsPredictions=" + squirlsPredictions +
                    '}';
        }
    }

}
