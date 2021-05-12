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

package org.monarchinitiative.squirls.cli.cmd.annotate_csv;

import de.charite.compbio.jannovar.annotation.Annotation;
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
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.monarchinitiative.squirls.cli.Main;
import org.monarchinitiative.squirls.cli.cmd.AnnotatingSquirlsCommand;
import org.monarchinitiative.squirls.cli.writers.*;
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.SquirlsException;
import org.monarchinitiative.squirls.core.SquirlsResult;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Daniel Danis
 */
@CommandLine.Command(name = "annotate-csv",
        aliases = {"C"},
        header = "Annotate variants stored in tabular file",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
public class AnnotateCsvCommand extends AnnotatingSquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateCsvCommand.class);

    private static final List<String> EXPECTED_HEADER = List.of("CHROM", "POS", "REF", "ALT");

    @CommandLine.Parameters(index = "1",
            paramLabel = "hg38_refseq.ser",
            description = "Path to Jannovar transcript database")
    public Path jannovarDataPath;

    @CommandLine.Parameters(index = "2",
            paramLabel = "input.csv",
            description = "Path to the input tabular file")
    public Path inputPath;

    @CommandLine.Parameters(index = "3",
            paramLabel = "path/to/output",
            description = "Prefix for the output files")
    public String outputPrefix;

    private static Variant parseCsvRecord(GenomicAssembly assembly, CSVRecord record) throws SquirlsException {
        String chrom = record.get("CHROM");
        Contig contig = assembly.contigByName(chrom);
        if (contig.isUnknown())
            throw new SquirlsException("Unknown contig `" + chrom + "` in record `" + record + "`");

        int pos;
        try {
            pos = Integer.parseInt(record.get("POS"));
        } catch (NumberFormatException e) {
            throw new SquirlsException("Invalid pos `" + record.get("POS") + " in record `" + record + "`", e);
        }

        String ref = record.get("REF");
        String alt = record.get("ALT");
        return Variant.of(contig, "", Strand.POSITIVE, CoordinateSystem.oneBased(), Position.of(pos), ref, alt);
    }

    private static VariantAnnotations annotateWithJannovar(VariantAnnotator annotator, ReferenceDictionary rd, Variant variant) throws AnnotationException, RuntimeException {
        Integer contigId = rd.getContigNameToID().get(variant.contigName());
        if (contigId == null)
            throw new AnnotationException("Unknown contig " + variant.contigName());

        GenomePosition gp = new GenomePosition(rd, de.charite.compbio.jannovar.reference.Strand.FWD, contigId, variant.startWithCoordinateSystem(CoordinateSystem.oneBased()), PositionType.ONE_BASED);
        GenomeVariant gv = new GenomeVariant(gp, variant.ref(), variant.alt());

        return annotator.buildAnnotations(gv);
    }

    @Override
    public Integer call() {
        LOGGER.info("Reading variants from `{}`", inputPath.toAbsolutePath());

        JannovarData jd;
        try {
            LOGGER.info("Loading transcript database from `{}`", jannovarDataPath.toAbsolutePath());
            jd = new JannovarDataSerializer(jannovarDataPath.toAbsolutePath().toString()).load();
        } catch (SerializationException e) {
            LOGGER.error("Unable to deserialize jannovar transcript database: {}", e.getMessage());
            return 1;
        }

        try (ConfigurableApplicationContext context = getContext()) {
            VariantAnnotator annotator = new VariantAnnotator(jd.getRefDict(), jd.getChromosomes(), new AnnotationBuilderOptions());
            VariantSplicingEvaluator evaluator = context.getBean(VariantSplicingEvaluator.class);
            SquirlsDataService dataService = context.getBean(SquirlsDataService.class);
            GenomicAssembly assembly = dataService.genomicAssembly();
            // ensure the fail-fast behavior at the cost of being retrieved far from the usage
            AnalysisResultsWriter analysisResultsWriter = context.getBean(AnalysisResultsWriter.class);

            int allVariants = 0;
            int annotatedAlleleCount = 0;
            List<WritableSplicingAllele> annotated = new LinkedList<>();
            try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                    .parse(Files.newBufferedReader(inputPath))) {

                // check
                if (!parser.getHeaderNames().containsAll(EXPECTED_HEADER)) {
                    LOGGER.warn("The input file header does not contain the required columns");
                    return 1;
                }

                // iterate through rows of the tabular file
                for (CSVRecord record : parser) {
                    allVariants++;

                    Variant variant;
                    try {
                        variant = parseCsvRecord(assembly, record);
                    } catch (SquirlsException e) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("line #{}: {}", record.getRecordNumber(), e.getMessage());
                        continue;
                    }

                    VariantAnnotations annotations;
                    try {
                        annotations = annotateWithJannovar(annotator, jd.getRefDict(), variant);
                    } catch (AnnotationException | RuntimeException e) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("line #{}: {}", record.getRecordNumber(), e.getMessage());
                        continue;
                    }

                    Set<String> txAccessionIds = annotations.getAnnotations().stream()
                            .map(Annotation::getTranscript)
                            .map(TranscriptModel::getAccession)
                            .collect(Collectors.toUnmodifiableSet());

                    SquirlsResult squirlsResult = evaluator.evaluate(variant, txAccessionIds);

                    WritableSplicingAllele allele = WritableSplicingAlleleDefault.of(variant, annotations, squirlsResult);
                    annotated.add(allele);
                    annotatedAlleleCount++;
                }
            } catch (IOException e) {
                LOGGER.warn("Error reading input", e);
                return 1;
            }

            // write out the results
            AnalysisResults results = AnalysisResults.builder()
                    .analysisStats(AnalysisStats.of(allVariants, allVariants, annotatedAlleleCount))
                    .settingsData(SettingsData.builder()
                            .inputPath(inputPath.toAbsolutePath().toString())
                            .transcriptDb(jannovarDataPath.toAbsolutePath().toString())
                            .nReported(nVariantsToReport)
                            .build())
                    .addAllVariants(annotated)
                    .build();

            analysisResultsWriter.writeResults(results, prepareOutputOptions(outputPrefix));
        }

        return 0;
    }
}
