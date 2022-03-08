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
 * Daniel Danis, Peter N Robinson, 2021
 */

package org.monarchinitiative.squirls.cli.cmd.precalculate;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.monarchinitiative.squirls.cli.Main;
import org.monarchinitiative.squirls.cli.cmd.ProgressReporter;
import org.monarchinitiative.squirls.cli.cmd.SquirlsCommand;
import org.monarchinitiative.squirls.cli.cmd.SquirlsWorkerThread;
import org.monarchinitiative.squirls.core.*;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.assembly.SequenceRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Daniel Danis
 */
@CommandLine.Command(name = "precalculate",
        aliases = {"E"},
        header = "Precalculate SQUIRLS scores for provided regions and store results in a VCF file",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
public class PrecalculateCommand extends SquirlsCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrecalculateCommand.class);

    private static final NumberFormat NF = NumberFormat.getInstance();

    // process at most 1 region within a task
    private static final int GRANULARITY = 1;

    private static final Thread.UncaughtExceptionHandler HANDLER = (thread, throwable) ->
            LOGGER.error("Error on thread {}: {}", thread.getName(), throwable.getMessage());
    private static final Pattern REGION_PATTERN = Pattern.compile("^(?<contig>[\\w._]+):(?<start>\\d+)-(?<end>\\d+)$");

    @CommandLine.Option(names = {"-i", "--input"},
            description = "Path to BED file with definitions of query regions")
    public Path inputFilePath;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Where to write the scores (default: ${DEFAULT-VALUE})")
    public Path outputPath = Path.of("squirls-scores.vcf.gz");

    @CommandLine.Option(names = {"-t", "--n-threads"},
            paramLabel = "2",
            description = "Process variants on n threads (default: ${DEFAULT-VALUE})")
    public int nThreads = 2;

    @CommandLine.Option(names = {"-l", "--length"},
            description = "Maximum length of generated variants (default: ${DEFAULT-VALUE})")
    public int length = 1;

    @CommandLine.Option(names = {"--individual"},
            description = "Write out predictions made with respect to individual transcripts (default: ${DEFAULT-VALUE})")
    public boolean writeIndividualPredictions = false;

    @CommandLine.Parameters(index = "1..*",
            paramLabel = "chr1:1000-2000",
            description = "Regions to precalculate the scores for")
    public List<String> regions = List.of();


    @Override
    public Integer call() {
        if (nThreads < 1) {
            LOGGER.error("Thread number must be a positive integer: {}", nThreads);
            return 1;
        }

        int processorsAvailable = Runtime.getRuntime().availableProcessors();
        if (nThreads > processorsAvailable)
            LOGGER.warn("You asked for more threads ({}) than there are processors ({}) available on the system", nThreads, processorsAvailable);

        LOGGER.info("Processing variants on {} threads", nThreads);

        if (length < 1) {
            LOGGER.error("Maximum length must be a positive integer: {}", length);
            return 1;
        }
        LOGGER.info("Writing variants up to {}bp long", length);

        try (ConfigurableApplicationContext context = getContext()) {
            Squirls squirls = getSquirls(context);
            SquirlsDataService squirlsDataService = squirls.squirlsDataService();
            GenomicAssembly assembly = squirlsDataService.genomicAssembly();
            List<GenomicRegion> regions = prepareGenomicRegions(assembly);

            Map<Integer, List<GenomicRegion>> regionByContig = regions.stream()
                    .collect(Collectors.groupingBy(GenomicRegion::contigId, Collectors.toUnmodifiableList()));



            VariantGenerator generator = new VariantGenerator(length);
            VariantContextAdaptor adaptor = new VariantContextAdaptor(writeIndividualPredictions, squirlsDataService);

            LOGGER.info("Writing scores to `{}`", outputPath.toAbsolutePath());
            try (VariantContextWriter writer = new VariantContextWriterBuilder()
                    .setOutputPath(outputPath)
                    .setReferenceDictionary(prepareSequenceDictionary(assembly))
                    .setOptions(EnumSet.of(Options.USE_ASYNC_IO, Options.DO_NOT_WRITE_GENOTYPES))
                    .build()) {

                VCFHeader header = prepareHeader(assembly);
                writer.writeHeader(header);

                ProgressReporter progressReporter = new ProgressReporter(10_000);
                ForkJoinPool pool = new ForkJoinPool(nThreads, SquirlsWorkerThread::new, HANDLER, true);
                for (int contigId : regionByContig.keySet()) {
                    List<GenomicRegion> contigRegions = regionByContig.get(contigId);
                    if (contigRegions.isEmpty())
                        continue;

                    List<GenomicRegion> preprocessed = RegionUtils.mergeOverlapping(contigRegions);

                    int baseCount = preprocessed.stream().mapToInt(Region::length).sum();
                    String contigName = assembly.contigById(contigId).name();
                    LOGGER.info("Precalculating scores for {} positions of chromosome {}", NF.format(baseCount), contigName);

                    Precalculation precalculation = Precalculation.of(preprocessed, generator, adaptor, squirlsDataService, squirls.splicingAnnotator(), squirls.squirlsClassifier(), writer, GRANULARITY, progressReporter);
                    pool.submit(precalculation);
                }
                pool.shutdown();

                //noinspection StatementWithEmptyBody
                while (!pool.awaitTermination(2, TimeUnit.SECONDS)) {}

            }
        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage(), e);
            return 1;
        }

        LOGGER.info("Results wrote to `{}`", outputPath.toAbsolutePath());
        LOGGER.info("Precalculating finished successfully. Bye.");
        return 0;
    }

    private static VCFHeader prepareHeader(GenomicAssembly assembly) {
        VCFHeader header = new VCFHeader();
        header.setVCFHeaderVersion(VCFHeaderVersion.VCF4_2);

        header.addMetaDataLine(new VCFHeaderLine("reference", assembly.name() + '(' + assembly.genBankAccession() + ')'));

        for (Contig contig : assembly.contigs()) {
            if (!contig.sequenceRole().equals(SequenceRole.ASSEMBLED_MOLECULE))
                continue;
            Map<String, String> contigMetadata = Map.of("ID", contig.name(), "GenBank", contig.genBankAccession(), "RefSeq", contig.refSeqAccession(),
                    "UCSC", contig.ucscName(), "length", String.valueOf(contig.length()));
            header.addMetaDataLine(new VCFContigHeaderLine(contigMetadata, contig.id()));
        }

        VariantContextAdaptor.headerLines().forEach(header::addMetaDataLine);

        return header;
    }

    private static SAMSequenceDictionary prepareSequenceDictionary(GenomicAssembly assembly) {
        List<SAMSequenceRecord> records = assembly.contigs().stream()
                .map(c -> new SAMSequenceRecord(c.name(), c.length()))
                .collect(Collectors.toUnmodifiableList());
        return new SAMSequenceDictionary(records);
    }


    private List<GenomicRegion> prepareGenomicRegions(GenomicAssembly assembly) {
        List<GenomicRegion> regions = new LinkedList<>();

        regions.addAll(parseRegionsProvidedAsCliParameters(assembly));
        regions.addAll(parseRegionsProvidedViaBedFile(assembly));

        return regions;
    }

    private Collection<GenomicRegion> parseRegionsProvidedAsCliParameters(GenomicAssembly assembly) {
        return regions.stream()
                .map(parseCliRegion(assembly))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableList());
    }

    private Collection<GenomicRegion> parseRegionsProvidedViaBedFile(GenomicAssembly assembly) {
        if (inputFilePath == null)
            return List.of();

        try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
            return reader.lines()
                    .filter(line -> !line.startsWith("#")) // remove header lines
                    .map(parseBedRegion(assembly))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toUnmodifiableList());
        } catch (IOException e) {
            LOGGER.warn("Error reading BED file at `{}`: {}", inputFilePath.toAbsolutePath(), e.getMessage());
            return List.of();
        }
    }

    private static Function<String, Optional<GenomicRegion>> parseBedRegion(GenomicAssembly assembly) {
        return line -> {
            String[] column = line.split("\\t", 3);
            if (column.length < 3) {
                if (LOGGER.isWarnEnabled()) LOGGER.warn("Less than 3 fields in BED line `{}`", line);
                return Optional.empty();
            }

            return parseData(column[0], column[1], column[2], assembly);
        };
    }

    private static Function<String, Optional<GenomicRegion>> parseCliRegion(GenomicAssembly assembly) {
        return line -> {
            Matcher matcher = REGION_PATTERN.matcher(line);
            if (matcher.matches()) {
                return parseData(matcher.group("contig"), matcher.group("start"), matcher.group("end"), assembly);
            } else {
                if (LOGGER.isWarnEnabled()) LOGGER.warn("Ignoring non-parsable record `{}`", line);
                return Optional.empty();
            }
        };
    }

    private static Optional<GenomicRegion> parseData(String contigName, String startString, String endString, GenomicAssembly assembly) {
        Contig contig = assembly.contigByName(contigName);
        if (contig.isUnknown()) {
            if (LOGGER.isWarnEnabled()) LOGGER.warn("Unknown contig `{}`", contigName);
            return Optional.empty();
        }

        int start, end;
        try {
            start = Integer.parseInt(startString);
            end = Integer.parseInt(endString);
        } catch (NumberFormatException e) {
            // Integer overflow
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("One of the coordinates was an unusually large number. Please report this to developers");
            return Optional.empty();
        }
        if (end < start) {
            // Would lead to an exception downstream, let's report it here in a more civilized fashion
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Skipping invalid line where start ({}), was greater than end ({})`", start, end);
            return Optional.empty();
        }


        return Optional.of(GenomicRegion.of(contig, Strand.POSITIVE, CoordinateSystem.zeroBased(), start, end));
    }

}
