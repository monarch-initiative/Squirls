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

package org.monarchinitiative.squirls.bootstrap;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apiguardian.api.API;
import org.monarchinitiative.sgenes.jannovar.JannovarParser;
import org.monarchinitiative.sgenes.model.Gene;
import org.monarchinitiative.squirls.core.Squirls;
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.config.SquirlsOptions;
import org.monarchinitiative.squirls.core.reference.SplicingPwmData;
import org.monarchinitiative.squirls.core.reference.StrandedSequenceService;
import org.monarchinitiative.squirls.core.reference.TranscriptModelService;
import org.monarchinitiative.squirls.core.scoring.AGEZSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.DenseSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.initialize.*;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;
import org.monarchinitiative.squirls.io.SquirlsResourceException;
import org.monarchinitiative.squirls.io.db.DbClassifierFactory;
import org.monarchinitiative.squirls.io.db.DbKMerDao;
import org.monarchinitiative.squirls.io.db.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.io.sequence.FastaStrandedSequenceService;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * This class prepares {@link Squirls} using files in given {@code dataDirectory}, {@link SquirlsProperties} and {@link SquirlsOptions}.
 * <p>
 * The Squirls {@code dataDirectory} is expected to have the following structure:
 * <pre>
 * path/to/squirls/data_directory:
 *  |- assembly_report.txt
 *  |- genome.fa
 *  |- genome.fa.dict
 *  |- genome.fa.fai
 *  |- phylop.bw
 *  |- squirls.mv.db
 *  |- tx.ensembl.ser
 *  |- tx.refseq.ser
 *  \- tx.ucsc.ser
 * </pre>
 * <p>
 * In the example above, <code>path/to/squirls/data_directory</code> should be provided as {@code dataDirectory}.
 *
 * <p>
 * Use {@link #getSquirls()} to get {@link Squirls} instance.
 *
 * @since 2.0.0
 * @author Daniel Danis
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public class SquirlsConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsConfigurationFactory.class);

    private static final Properties PROPERTIES = readProperties();

    private static final String SQUIRLS_VERSION = PROPERTIES.getProperty("squirls.version", "unknown version");

    private final SquirlsDataResolver dataResolver;

    private final SquirlsProperties properties;

    private final SquirlsOptions options;

    public static SquirlsConfigurationFactory of(Path dataDirectory,
                                                 SquirlsProperties properties,
                                                 SquirlsOptions options) throws MissingSquirlsResourceException, UndefinedSquirlsResourceException {
        return new SquirlsConfigurationFactory(dataDirectory, properties, options);
    }

    private SquirlsConfigurationFactory(Path dataDirectory,
                                        SquirlsProperties properties,
                                        SquirlsOptions options) throws MissingSquirlsResourceException, UndefinedSquirlsResourceException {
        this.dataResolver = SquirlsDataResolver.of(dataDirectory);
        this.properties = Objects.requireNonNull(properties, "Squirls properties must not be null");
        this.options = Objects.requireNonNull(options, "Squirls options must not be null");
    }

    private static Squirls configure(SquirlsDataResolver dataResolver,
                                     SquirlsProperties properties,
                                     SquirlsOptions options) throws IOException, SquirlsResourceException {
        LOGGER.info("Spooling up Squirls v{} using resources in `{}`", SQUIRLS_VERSION, dataResolver.dataDirectory().toAbsolutePath());
        DataSource squirlsDatasource = squirlsDatasource(dataResolver.dataSourcePath());

        SquirlsDataService squirlsDataService = configureSquirlsDataService(options, dataResolver);

        BigWigAccessor phylopBigwigAccessor = new BigWigAccessor(dataResolver.phylopPath());
        SplicingAnnotator splicingAnnotator = configureSplicingAnnotator(properties, squirlsDatasource, phylopBigwigAccessor);

        SquirlsClassifier squirlsClassifier = configureSquirlsClassifier(properties, squirlsDatasource);
        VariantSplicingEvaluator variantSplicingEvaluator = VariantSplicingEvaluator.of(squirlsDataService, splicingAnnotator, squirlsClassifier);

        return Squirls.of(squirlsDataService, splicingAnnotator, squirlsClassifier, variantSplicingEvaluator);
    }

    private static DataSource squirlsDatasource(Path datasourcePath) {
        String jdbcUrl = String.format("jdbc:h2:file:%s;ACCESS_MODE_DATA=r", datasourcePath.toAbsolutePath());
        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setPoolName("squirls-pool");

        return new HikariDataSource(config);
    }

    private static SquirlsDataService configureSquirlsDataService(SquirlsOptions options,
                                                                  SquirlsDataResolver dataResolver) throws SquirlsResourceException {
        StrandedSequenceService strandedSequenceService = new FastaStrandedSequenceService(dataResolver.genomeAssemblyReportPath(),
                dataResolver.genomeFastaPath(),
                dataResolver.genomeFastaFaiPath(),
                dataResolver.genomeFastaDictPath());

        TranscriptModelService transcriptModelService = configureTranscriptModelService(options,
                strandedSequenceService.genomicAssembly(),
                dataResolver);

        return SquirlsDataService.of(strandedSequenceService, transcriptModelService);
    }

    private static TranscriptModelService configureTranscriptModelService(SquirlsOptions options,
                                                                          GenomicAssembly genomicAssembly,
                                                                          SquirlsDataResolver dataResolver) throws SquirlsResourceException {
        Path jannovarSerPath;
        switch (options.featureSource()) {
            case REFSEQ:
                jannovarSerPath = dataResolver.refseqSerPath();
                break;
            case ENSEMBL:
                jannovarSerPath = dataResolver.ensemblSerPath();
                break;
            case UCSC:
                jannovarSerPath = dataResolver.ucscSerPath();
                break;
            default:
                throw new SquirlsResourceException("Unknown transcript source `" + options.featureSource() + "`");
        }

        return TranscriptModelService.of(readGenes(genomicAssembly, jannovarSerPath.toAbsolutePath()));
    }

    private static List<? extends Gene> readGenes(GenomicAssembly assembly, Path jannovarSerPath) {
        Objects.requireNonNull(assembly, "Assembly must not be null");

        JannovarParser parser = JannovarParser.of(jannovarSerPath, assembly);
        return parser.stream()
                .collect(Collectors.toUnmodifiableList());
    }

    private static SplicingAnnotator configureSplicingAnnotator(SquirlsProperties properties,
                                                                DataSource squirlsDatasource,
                                                                BigWigAccessor phylopBigwigAccessor) throws SquirlsResourceException {
        SplicingPwmData splicingPwmData = new DbSplicingPositionalWeightMatrixParser(squirlsDatasource).getSplicingPwmData();
        DbKMerDao dbKMerDao = new DbKMerDao(squirlsDatasource);
        AnnotatorProperties annotatorProperties = properties.getAnnotator();
        String version = annotatorProperties.getVersion();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Using `{}` splicing annotator", version);
        switch (version) {
            case "dense":
                return new DenseSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap(), phylopBigwigAccessor);
            case "agez":
                return new AGEZSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap(), phylopBigwigAccessor);
            default:
                throw new SquirlsResourceException(String.format("invalid 'squirls.annotator.version' property value: `%s`", version));
        }
    }

    private static SquirlsClassifier configureSquirlsClassifier(SquirlsProperties properties,
                                                                DataSource squirlsDatasource) throws SquirlsResourceException {
        DbClassifierFactory classifierFactory = new DbClassifierFactory(squirlsDatasource);
        ClassifierProperties classifierProperties = properties.getClassifier();

        SquirlsClassifierVersion clfVersion;
        try {
            clfVersion = SquirlsClassifierVersion.parseString(classifierProperties.getVersion());
        } catch (IllegalArgumentException e) {
            throw new UndefinedSquirlsResourceException(e.getMessage(), e);
        }
        Collection<SquirlsClassifierVersion> avail = classifierFactory.getAvailableClassifiers();
        if (!avail.contains(clfVersion)) {
            String msg = String.format("Classifier version `%s` is not available, choose one from %s",
                    clfVersion,
                    avail.stream().map(Objects::toString).sorted().collect(Collectors.joining(", ", "{", "}")));
            if (LOGGER.isErrorEnabled()) LOGGER.error(msg);
            throw new UndefinedSquirlsResourceException(msg);
        }

        // get classifier
        Optional<SquirlsClassifier> clfOpt = classifierFactory.readClassifier(clfVersion);
        if (clfOpt.isPresent()) {
            LOGGER.debug("Using classifier `{}`", clfVersion);
            return clfOpt.get();
        } else {
            String msg = String.format("Error when deserializing classifier `%s` from the database", clfVersion);
            throw new SquirlsResourceException(msg);
        }
    }

    private static Properties readProperties() {
        Properties properties = new Properties();

        try (InputStream is = SquirlsConfigurationFactory.class.getResourceAsStream("/squirls.properties")) {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.warn("Error loading properties: {}", e.getMessage());
        }
        return properties;
    }

    /**
     * @param resourceVersion resource version
     * @return configuration with
     * @throws SquirlsResourceException in case there are any issues with initialization (e.g. missing files)
     */
    public Squirls getSquirls() throws SquirlsResourceException {
        try {
            return configure(dataResolver, properties, options);
        } catch (IOException e) {
            throw new SquirlsResourceException(e);
        }
    }

}
