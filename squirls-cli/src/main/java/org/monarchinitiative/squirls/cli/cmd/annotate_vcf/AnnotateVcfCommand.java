package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.cli.writers.*;
import org.monarchinitiative.squirls.core.SplicingPredictionData;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@CommandLine.Command(name = "annotate-vcf", aliases = {"A"}, mixinStandardHelpOptions = true,
        description = "annotate variants in a VCF file")
public class AnnotateVcfCommand extends SquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateVcfCommand.class);

    @CommandLine.Option(names = {"-d", "--jannovar-data"},
            required = true,
            description = "path to Jannovar transcript database")
    public String jannovarDataPath;

    @CommandLine.Option(names = {"-f", "--output-format"},
            description = "comma separated list of output formats to use for writing the results [html,VCF]")
    public String outputFormats = "HTML";

    @CommandLine.Option(names = {"-n", "--n-variants-to-report"},
            defaultValue = "100",
            description = "N most pathogenic variants to include into HTML report")
    public int nVariantsToReport;

    @CommandLine.Parameters(index = "0", description = "path to input VCF file")
    public Path inputPath;

    @CommandLine.Parameters(index = "1", description = "prefix for the output files")
    public String outputPrefix;

    /**
     * Split {@link VariantContext} into <em>alt</em> alleles and annotate each allele with Squirls and Jannovar.
     *
     * @param evaluator variant splicing evaluator to use
     * @param rd        Jannovar's reference dictionary
     * @param annotator Jannovar's variant annotator
     * @return annotated {@link VariantContext}
     */
    private static Function<VariantContext, Collection<WritableSplicingAllele>> annotateVariant(VariantSplicingEvaluator evaluator,
                                                                                                ReferenceDictionary rd,
                                                                                                VariantAnnotator annotator) {
        return vc -> {
            List<WritableSplicingAllele> evaluations = new ArrayList<>(vc.getAlternateAlleles().size());
            for (Allele allele : vc.getAlternateAlleles()) {
                // jannovar annotations
                Integer contigId = rd.getContigNameToID().get(vc.getContig());
                if (contigId == null) {
                    LOGGER.warn("Jannovar does not recognize contig {} for variant {}", vc.getContig(), vc);
                    continue;
                }

                GenomePosition pos = new GenomePosition(rd, Strand.FWD, contigId, vc.getStart(), PositionType.ONE_BASED);
                GenomeVariant variant = new GenomeVariant(pos, vc.getReference().getDisplayString(), allele.getDisplayString());
                VariantAnnotations variantAnnotations;
                try {
                    variantAnnotations = annotator.buildAnnotations(variant);
                } catch (AnnotationException e) {
                    LOGGER.warn("Unable to annotate variant {}: {}", variant, e.getMessage());
                    continue;
                }

                // Squirls scores
                Map<String, SplicingPredictionData> squirlsScores = variantAnnotations.getHighestImpactEffect().isOffTranscript()
                        ? Map.of() // don't bother with annotating an off-exome variant
                        : evaluator.evaluate(vc.getContig(), vc.getStart(), vc.getReference().getBaseString(), allele.getBaseString());


                evaluations.add(new WritableSplicingAlleleImpl(vc, allele, variantAnnotations, squirlsScores));
            }

            return evaluations;
        };
    }

    @Override
    public Integer call() {
        try (ConfigurableApplicationContext context = getContext()) {
            LOGGER.info("Reading variants from `{}`", inputPath);
            Set<OutputFormat> outputFormats = parseOutputFormats();
            VariantSplicingEvaluator evaluator = context.getBean(VariantSplicingEvaluator.class);

            JannovarData jd;
            try {
                jd = new JannovarDataSerializer(jannovarDataPath).load();
            } catch (SerializationException e) {
                LOGGER.error("Unable to deserialize jannovar data at {}: {}", jannovarDataPath, e.getMessage());
                return 1;
            }
            VariantAnnotator annotator = new VariantAnnotator(jd.getRefDict(),
                    jd.getChromosomes(),
                    new AnnotationBuilderOptions());

            // TODO: 29. 5. 2020 improve behavior & logging
            // e.g. report progress in % if variant index and thus count is available
            AnnotateVcfProgressReporter progressReporter = new AnnotateVcfProgressReporter(5_000);
            List<WritableSplicingAllele> annotated = Collections.synchronizedList(new ArrayList<>());

            // annotate the variants
            LOGGER.info("Annotating variants");
            try (VCFFileReader reader = new VCFFileReader(inputPath, false);
                 CloseableIterator<VariantContext> variantIterator = reader.iterator()) {

                try (Stream<VariantContext> stream = variantIterator.stream()) {
                    stream.parallel()
                            .onClose(progressReporter.summarize())
                            .peek(progressReporter::logItem)

                            .map(annotateVariant(evaluator, jd.getRefDict(), annotator))
                            .flatMap(Collection::stream)
                            .peek(progressReporter::logAltAllele)

                            .forEach(annotated::add);
                }
            }

            // write out the results
            AnalysisResults results = AnalysisResults.builder()
                    .settingsData(SettingsData.builder()
                            .inputPath(inputPath.toString())
                            .transcriptDb(jannovarDataPath)
                            .build())
                    .analysisStats(progressReporter.getAnalysisStats())
                    .variants(annotated)
                    .build();

            ResultWriterFactory resultWriterFactory = context.getBean(ResultWriterFactory.class);
            OutputSettings settings = new OutputSettings(outputPrefix, nVariantsToReport);
            for (OutputFormat format : outputFormats) {
                LOGGER.info("Writing out the {} results", format);
                ResultWriter writer = resultWriterFactory.resultWriterForFormat(format);
                try {
                    writer.write(results, settings);
                } catch (IOException e) {
                    LOGGER.warn("Error writing {} results: {}", format, e.getMessage());
                }
            }
        }

        return 0;
    }

    private Set<OutputFormat> parseOutputFormats() {
        Set<OutputFormat> formats = new HashSet<>(2);
        for (String format : outputFormats.split(",")) {
            try {
                formats.add(OutputFormat.valueOf(format.toUpperCase()));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid output format `{}`", format);
            }
        }
        return formats;
    }
}
