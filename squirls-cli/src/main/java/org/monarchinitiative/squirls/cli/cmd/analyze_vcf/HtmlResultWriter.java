package org.monarchinitiative.squirls.cli.cmd.analyze_vcf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * This class takes {@link AnalysisResults}, processes the content into HTML format using the appropriate template and
 * writes the HTML file into provided {@link OutputStream}.
 */
public class HtmlResultWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlResultWriter.class);

    private final TemplateEngine templateEngine;

    public HtmlResultWriter() {
        Locale.setDefault(Locale.US);
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public void writeResults(OutputStream os, AnalysisResults results) throws IOException {
        try (Writer writer = new OutputStreamWriter(os)) {

            final Context context = new Context();
            context.setVariable("sampleName", String.join(", ", results.getSampleNames()));
            context.setVariable("settings", results.getSettingsData());
            context.setVariable("stats", results.getAnalysisStats());
            context.setVariable("variants", results.getVariantData());

            templateEngine.process("results", context, writer);
        }
    }
}
