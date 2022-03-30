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

package org.monarchinitiative.squirls.cli.writers.tabular;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.squirls.cli.writers.AnalysisResults;
import org.monarchinitiative.squirls.cli.writers.ResultWriter;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.monarchinitiative.squirls.core.SquirlsResult;
import org.monarchinitiative.squirls.core.SquirlsTxResult;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicVariant;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Writer for storing <em>n</em> most pathogenic variants in a tabular format.
 *
 * @author Daniel Danis
 */
public class TabularResultWriter implements ResultWriter {

    private final String fileExtension;

    private final char columnSeparator;

    private final boolean compress;

    private final boolean reportTranscripts;

    private final boolean reportFeatures;

    public TabularResultWriter(String fileExtension,
                               char columnSeparator,
                               boolean compress,
                               boolean reportTranscripts,
                               boolean reportFeatures) {
        this.fileExtension = fileExtension;
        this.columnSeparator = columnSeparator;
        this.compress = compress;
        this.reportTranscripts = reportTranscripts;
        this.reportFeatures = reportFeatures;
    }

    private static String summarizeSquirlsFeatures(SquirlsResult squirlsResult) {
        return squirlsResult.results()
                .sorted(Comparator.comparing(SquirlsTxResult::accessionId))
                .map(summarizeTranscriptFeatures())
                .collect(Collectors.joining(";"));
    }

    private static Function<SquirlsTxResult, String> summarizeTranscriptFeatures() {
        return tx -> tx.features().entrySet().stream()
                .map(e -> e.getKey() + '=' + e.getValue())
                .collect(Collectors.joining("|", tx.accessionId() + '[', "]"));
    }

    private static String summarizeTranscriptsScores(SquirlsResult result) {
        return result.results()
                .sorted(Comparator.comparing(SquirlsTxResult::accessionId))
                .map(tx -> tx.accessionId() + "=" + tx.prediction().getMaxPathogenicity())
                .collect(Collectors.joining("|"));
    }

    @Override
    public void write(AnalysisResults results, Path prefix) throws IOException {
        String output = prefix.toAbsolutePath().toString() + '.' + fileExtension + (compress ? ".gz" : "");
        Path outputPath = Paths.get(output);
        LOGGER.info("Writing tabular output to `{}`", outputPath);

        List<String> header = new ArrayList<>(
                List.of("id", "chrom", "pos", "ref", "alt", "gene_symbol", "tx_accession", "interpretation", "squirls_score"));
        if (reportTranscripts)
            header.add("transcripts");

        if (reportFeatures)
            header.add("squirls_features");

        try (CSVPrinter printer = CSVFormat.newFormat(columnSeparator)
                .withRecordSeparator('\n')
                .withHeader(header.toArray(String[]::new))
                .print(openWriter(outputPath))) {
            results.getVariants().stream()
                    .sorted(Comparator.comparing(WritableSplicingAllele::maxSquirlsScore).reversed())
                    .limit(results.getSettingsData().getNReported())
                    .forEachOrdered(writeAllele(printer));
        }
    }

    private BufferedWriter openWriter(Path outputPath) throws IOException {
        return compress
                ? new BufferedWriter(new OutputStreamWriter(new GzipCompressorOutputStream(Files.newOutputStream(outputPath))))
                : Files.newBufferedWriter(outputPath);
    }

    private Consumer<WritableSplicingAllele> writeAllele(CSVPrinter printer) {
        return allele -> {
            // we write the following fields
            // "chrom", "id", "pos", "ref", "alt", "gene_symbol", "tx_accession", "interpretation", "squirls_score", "squirls_features" (optional)

            GenomicVariant variant = allele.variant();
            List<Object> columns = new LinkedList<>();
            columns.add(variant.id());
            columns.add(variant.contigName());
            columns.add(variant.startWithCoordinateSystem(CoordinateSystem.oneBased()));
            columns.add(variant.ref());
            columns.add(variant.alt());
            columns.add(allele.variantAnnotations().getHighestImpactAnnotation().getGeneSymbol());
            columns.add(allele.squirlsResult().maxPathogenicityTranscriptAccession().orElse("N/A"));
            columns.add(allele.squirlsResult().isPathogenic() ? "pathogenic" : "neutral");
            columns.add(allele.squirlsResult().maxPathogenicity());

            if (reportTranscripts)
                columns.add(summarizeTranscriptsScores(allele.squirlsResult()));

            if (reportFeatures)
                columns.add(summarizeSquirlsFeatures(allele.squirlsResult()));

            try {
                printer.printRecord(columns);
            } catch (IOException e) {
                LOGGER.warn("Error writing variant {}:{}{}>{}: {}",
                        variant.contigName(), variant.startWithCoordinateSystem(CoordinateSystem.oneBased()),
                        variant.ref(), variant.alt(),
                        e.getMessage());
            }
        };
    }
}
