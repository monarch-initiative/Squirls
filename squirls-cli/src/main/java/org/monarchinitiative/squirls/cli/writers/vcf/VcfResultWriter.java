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

package org.monarchinitiative.squirls.cli.writers.vcf;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import org.monarchinitiative.squirls.cli.writers.AnalysisResults;
import org.monarchinitiative.squirls.cli.writers.OutputFormat;
import org.monarchinitiative.squirls.cli.writers.ResultWriter;
import org.monarchinitiative.squirls.cli.writers.WritableSplicingAllele;
import org.monarchinitiative.squirls.core.SquirlsResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VcfResultWriter implements ResultWriter {

    private static final String SQUIRLS_FLAG_FIELD_NAME = "SQUIRLS";

    private static final VCFFilterHeaderLine FLAG_LINE = new VCFFilterHeaderLine(SQUIRLS_FLAG_FIELD_NAME,
            "Squirls considers the variant as pathogenic if the filter is present");

    private static final String SQUIRLS_SCORE_FIELD_NAME = "SQUIRLS_SCORE";

    private static final VCFInfoHeaderLine SCORE_LINE = new VCFInfoHeaderLine(
            SQUIRLS_SCORE_FIELD_NAME,
            VCFHeaderLineCount.A,
            VCFHeaderLineType.String,
            "Squirls pathogenicity score");

    /**
     * Extend the <code>header</code> with INFO fields that are being added in this command.
     *
     * @param header to extend
     * @return the extended header
     */
    private static VCFHeader extendHeaderWithSquirlsFields(VCFHeader header) {
        // SQUIRLS - flag
        header.addMetaDataLine(FLAG_LINE);
        // SQUIRLS_SCORE - float
        header.addMetaDataLine(SCORE_LINE);
        return header;
    }

    /**
     * Store Squirls predictions into VCF INFO fields.
     *
     * @return variant context with populated INFO fields
     */
    private static Function<WritableSplicingAllele, VariantContext> addInfoFields() {
        return ve -> {
            VariantContextBuilder builder = new VariantContextBuilder(ve.variantContext());
            SquirlsResult squirlsScores = ve.squirlsResult();

            if (squirlsScores.isEmpty()) {
                return builder.make();
            }

            // is the ALT allele pathogenic wrt any overlapping transcript?
            builder = squirlsScores.isPathogenic()
                    ? builder.filter(SQUIRLS_FLAG_FIELD_NAME)
                    : builder;

            // prediction string wrt all overlapping transcripts
            String txPredictions = squirlsScores.results()
                    // tx_accession=score
                    .map(sq -> String.format("%s=%f", sq.accessionId(), sq.prediction().getMaxPathogenicity()))
                    .collect(Collectors.joining("|", String.format("%s|", ve.allele().getBaseString()), ""));

            return builder.attribute(SQUIRLS_SCORE_FIELD_NAME, txPredictions).make();
        };
    }

    @Override
    public void write(AnalysisResults results, String prefix) throws IOException {
        Path inputVcfPath = Paths.get(results.getSettingsData().getInputPath());
        Path outputPath = Paths.get(prefix + '.' + OutputFormat.VCF.getFileExtension());
        LOGGER.info("Writing VCF output to `{}`", outputPath);

        VCFHeader header;
        try (VCFFileReader reader = new VCFFileReader(inputVcfPath, false)) {
            header = extendHeaderWithSquirlsFields(reader.getFileHeader());
        }

        try (VariantContextWriter writer = new VariantContextWriterBuilder()
                .setOutputPath(outputPath)
                .setReferenceDictionary(header.getSequenceDictionary())
                .setOutputFileType(VariantContextWriterBuilder.OutputType.VCF)
                .unsetOption(Options.INDEX_ON_THE_FLY)
                .build()) {

            writer.writeHeader(header);

            results.getVariants().stream()
                    .map(addInfoFields())
                    .sorted(header.getVCFRecordComparator())
                    .forEach(writer::add);
        }
    }
}
