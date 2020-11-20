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

package org.monarchinitiative.squirls.cli.cmd.annotate_vcf;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationException;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.*;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import org.monarchinitiative.squirls.cli.Main;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.cli.writers.*;
import org.monarchinitiative.squirls.core.SquirlsResult;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "annotate-vcf",
        aliases = {"A"},
        header = "Annotate variants in a VCF file",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
public class AnnotateVcfCommand extends SquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateVcfCommand.class);

    @CommandLine.Option(names = {"-d", "--jannovar-data"},
            required = true,
            paramLabel = "hg38_refseq.ser",
            description = "Path to Jannovar transcript database")
    public String jannovarDataPath;

    @CommandLine.Option(names = {"-f", "--output-format"},
            paramLabel = "html",
            description = "Comma separated list of output formats to use for writing the results")
    public String outputFormats = "html";

    @CommandLine.Option(names = {"-n", "--n-variants-to-report"},
            paramLabel = "100",
            description = "N most pathogenic variants to include into HTML report")
    public int nVariantsToReport = 100;

    @CommandLine.Option(names = {"-t", "--n-threads"},
            paramLabel = "4",
            description = "Process variants using n threads")
    public int nThreads = 4;

    @CommandLine.Parameters(index = "0",
            paramLabel = "input.vcf",
            description = "Path to input VCF file")
    public Path inputPath;

    @CommandLine.Parameters(index = "1",
            paramLabel = "path/to/output",
            description = "Prefix for the output files")
    public String outputPrefix;

    private static Function<VariantContext, Collection<VariantContext>> meltToSingleAltVariants() {
        return vc -> {
            List<Allele> alts = vc.getAlternateAlleles();
            List<VariantContext> contexts = new ArrayList<>(alts.size());
            for (Allele alt : alts) {
                contexts.add(new VariantContextBuilder(vc)
                        .alleles(List.<Allele>of()) // delete alleles
                        .alleles(List.of(vc.getReference(), alt))
                        .make());
            }
            return contexts;
        };
    }

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
                SquirlsResult squirlsResult;
                if (!variantAnnotations.getHighestImpactEffect().isOffTranscript()) {
                    Set<String> txAccessions = variantAnnotations.getAnnotations().stream()
                            .map(Annotation::getTranscript)
                            .map(TranscriptModel::getAccession)
                            .collect(Collectors.toSet());
                    squirlsResult = evaluator.evaluate(vc.getContig(), vc.getStart(), vc.getReference().getBaseString(), allele.getBaseString(), txAccessions);
                } else {
                    // don't bother with annotating an off-exome variant
                    squirlsResult = SquirlsResult.empty();
                }

                evaluations.add(new WritableSplicingAlleleDefault(vc, allele, variantAnnotations, squirlsResult));
            }

            return evaluations;
        };
    }

    private static ForkJoinPool makePool(int parallelism) {
        return new ForkJoinPool(parallelism, SquirlsWorkerThread::new, null, false);
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
            //  e.g. report progress in % if variant index and thus count is available
            AnnotateVcfProgressReporter progressReporter = new AnnotateVcfProgressReporter(5_000);
            List<WritableSplicingAllele> annotated; // = Collections.synchronizedList(new ArrayList<>());
            ArrayList<String> sampleNames;

            // annotate the variants
            int procsAvail = Runtime.getRuntime().availableProcessors();
            if (nThreads > procsAvail) {
                LOGGER.warn("You asked for more threads ({}) than processors ({}) available on the system", nThreads, procsAvail);
            }
            LOGGER.info("Annotating variants on {} threads", nThreads);
            try (VCFFileReader reader = new VCFFileReader(inputPath, false);
                 CloseableIterator<VariantContext> variantIterator = reader.iterator()) {

                sampleNames = reader.getFileHeader().getSampleNamesInOrder();

                ForkJoinPool pool = makePool(nThreads);
                try (Stream<VariantContext> stream = variantIterator.stream()) {
                    annotated = pool.submit(() ->
                            stream.parallel()
                                    .onClose(progressReporter.summarize())
                                    .peek(progressReporter::logVariant)

                                    .map(meltToSingleAltVariants())
                                    .flatMap(Collection::stream)
                                    .peek(progressReporter::logAllele)

                                    .map(annotateVariant(evaluator, jd.getRefDict(), annotator))
                                    .flatMap(Collection::stream)
                                    .peek(wa -> {
                                        if (!wa.squirlsResult().isEmpty()) {
                                            progressReporter.logAnnotatedAllele(wa);
                                        }
                                    })
                                    .collect(Collectors.toList())).get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.warn("Error: ", e);
                    return 1;
                }
                pool.shutdown();
            }

            // write out the results
            AnalysisResults results = AnalysisResults.builder()
                    .addAllSampleNames(sampleNames)
                    .settingsData(SettingsData.builder()
                            .inputPath(inputPath.toString())
                            .transcriptDb(jannovarDataPath)
                            .nReported(nVariantsToReport)
                            .build())
                    .analysisStats(progressReporter.getAnalysisStats())
                    .variants(annotated)
                    .build();

            ResultWriterFactory resultWriterFactory = context.getBean(ResultWriterFactory.class);
            OutputSettings settings = new OutputSettings(outputPrefix, nVariantsToReport);
            for (OutputFormat format : outputFormats) {
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
