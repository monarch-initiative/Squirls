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

package org.monarchinitiative.squirls.io.sequence;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.FastaSequenceIndex;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.variant.utils.SAMSequenceDictionaryExtractor;
import org.monarchinitiative.squirls.core.reference.StrandedSequence;
import org.monarchinitiative.squirls.core.reference.StrandedSequenceService;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.assembly.SequenceRole;
import org.monarchinitiative.svart.parsers.GenomicAssemblyParser;
import org.monarchinitiative.svart.util.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StrandedSequenceService} that uses HtsJDK to fetch sequence from a single indexed FASTA file
 * containing all contigs of the assembly.
 * @author Daniel Danis
 */
public class FastaStrandedSequenceService implements StrandedSequenceService, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastaStrandedSequenceService.class);

    private final GenomicAssembly assembly;

    private final IndexedFastaSequenceFile fasta;

    /**
     * True if all chromosomes in FASTA are prefixed with `chr` and false if all chromosomes are not prefixed.
     */
    private final boolean usesPrefix;

    public FastaStrandedSequenceService(Path assemblyReportPath, Path fastaPath, Path fastaFai, Path fastaDict) throws InvalidFastaFileException {
        this.assembly = GenomicAssemblyParser.parseAssembly(assemblyReportPath);
        this.fasta = new IndexedFastaSequenceFile(fastaPath, new FastaSequenceIndex(fastaFai));
        SAMSequenceDictionary sequenceDictionary = buildSequenceDictionary(fastaDict);
        this.usesPrefix = figureOutPrefix(sequenceDictionary);
        check(assembly, sequenceDictionary);
    }

    private static void check(GenomicAssembly assembly, SAMSequenceDictionary sequenceDictionary) throws InvalidFastaFileException {
        // we require assembly contigs with `SequenceRole.ASSEMBLED_MOLECULE` to be present in the FASTA file
        Set<String> assemblyContigNames = assembly.contigs().stream()
                .filter(c -> c.sequenceRole().equals(SequenceRole.ASSEMBLED_MOLECULE))
                .map(c -> List.of(c.name(), c.refSeqAccession(), c.genBankAccession(), c.ucscName()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Set<String> dictContigNames = sequenceDictionary.getSequences().stream()
                .map(SAMSequenceRecord::getSequenceName)
                .filter(name -> !(name.startsWith("chrUn") || name.contains("random") || name.contains("hap") || name.endsWith("alt")))
                .collect(Collectors.toSet());
        if (!assemblyContigNames.containsAll(dictContigNames)) {
            throw new InvalidFastaFileException("Required contigs are missing in FASTA file");
        }

        // check that contig lengths match
        Map<String, Integer> assemblyContigLengths = assemblyContigNames.stream()
                .collect(Collectors.toMap(Function.identity(), contigName -> assembly.contigByName(contigName).length()));

        Map<String, Integer> dictionaryContigLengths = dictContigNames.stream()
                .collect(Collectors.toMap(Function.identity(), contigName -> sequenceDictionary.getSequence(contigName).getSequenceLength()));

        boolean lengthMismatch = false;
        for (String dictContig : dictionaryContigLengths.keySet()) {
            int dictContigLength = dictionaryContigLengths.get(dictContig);
            int assemblyContigLength = assemblyContigLengths.get(dictContig);
            if (dictContigLength != assemblyContigLength) {
                LOGGER.warn("Contig length mismatch {}!={} between `{}` (genome assembly report)  and `{}` (FASTA sequence dictionary)", assemblyContigLength, dictContigLength, dictContig, dictContig);
                lengthMismatch = true;
            }
        }
        if (lengthMismatch) throw new InvalidFastaFileException("Contig length mismatch");
    }

    private static SAMSequenceDictionary buildSequenceDictionary(Path dictPath) {
        return SAMSequenceDictionaryExtractor.extractDictionary(dictPath);
    }

    private static boolean figureOutPrefix(SAMSequenceDictionary sequenceDictionary) throws InvalidFastaFileException {
        Predicate<SAMSequenceRecord> prefixed = e -> e.getSequenceName().startsWith("chr");
        boolean allPrefixed = sequenceDictionary.getSequences().stream().allMatch(prefixed);
        boolean nonePrefixed = sequenceDictionary.getSequences().stream().noneMatch(prefixed);

        if (allPrefixed) return true;
        else if (nonePrefixed) return false;
        else {
            String msg = String.format("Found prefixed and unprefixed contigs among fasta dictionary entries - %s",
                    sequenceDictionary.getSequences().stream()
                            .map(SAMSequenceRecord::getSequenceName).collect(Collectors.joining(",", "{", "}")));
            if (LOGGER.isErrorEnabled())
                LOGGER.error(msg);
            throw new InvalidFastaFileException(msg);
        }
    }

    @Override
    public GenomicAssembly genomicAssembly() {
        return assembly;
    }

    @Override
    public StrandedSequence sequenceForRegion(GenomicRegion region) {
        Contig contig = assembly.contigByName(region.contigName());
        if (contig.equals(Contig.unknown())) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Unknown chromosome `{}`", region.contigName());
            return null;
        }

        // the name we use for contig in FASTA file
        GenomicRegion onPositive = region.toPositiveStrand().toOneBased();
        String contigName = usesPrefix ? contig.ucscName() : contig.name();
        String seq;
        try {
            synchronized (this) {
                ReferenceSequence referenceSequence = fasta.getSubsequenceAt(contigName, onPositive.start(), onPositive.end());
                seq = new String(referenceSequence.getBases());
            }
        } catch (SAMException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Error getting sequence for query `{}:{}-{}`: {}", onPositive.contigName(), onPositive.start(), onPositive.end(), e.getMessage());
            return null;
        }
        return StrandedSequence.of(region, region.strand().isPositive() ? seq : Seq.reverseComplement(seq));
    }

    @Override
    public void close() throws Exception {
        fasta.close();
    }
}
