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

package org.monarchinitiative.squirls.autoconfigure;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.monarchinitiative.squirls.autoconfigure.exception.InvalidSquirlsResourceException;
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
import org.monarchinitiative.squirls.core.reference.SplicingPwmData;
import org.monarchinitiative.squirls.core.reference.StrandedSequenceService;
import org.monarchinitiative.squirls.core.reference.TranscriptModelService;
import org.monarchinitiative.squirls.core.reference.TranscriptModelServiceOptions;
import org.monarchinitiative.squirls.core.scoring.AGEZSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.DenseSplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.SplicingAnnotator;
import org.monarchinitiative.squirls.core.scoring.calculators.conservation.BigWigAccessor;
import org.monarchinitiative.squirls.initialize.*;
import org.monarchinitiative.squirls.initialize.UndefinedSquirlsResourceException;
import org.monarchinitiative.squirls.io.ClassifierFactory;
import org.monarchinitiative.squirls.io.CorruptedPwmException;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;
import org.monarchinitiative.squirls.io.SquirlsResourceException;
import org.monarchinitiative.squirls.io.db.DbClassifierFactory;
import org.monarchinitiative.squirls.io.db.DbKMerDao;
import org.monarchinitiative.squirls.io.db.DbSplicingPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.io.db.TranscriptModelServiceDb;
import org.monarchinitiative.squirls.io.sequence.FastaStrandedSequenceService;
import org.monarchinitiative.squirls.io.sequence.InvalidFastaFileException;
import org.monarchinitiative.svart.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;


/**
 * This class assembles Squirls high-level classes from the inputs.
 * <p>
 * The autoconfiguration requires specification of the following properties:
 *     <ul>
 *         <li><code>squirls.data-directory</code></li>
 *         <li><code>squirls.genome-assembly</code></li>
 *         <li><code>squirls.data-version</code></li>
 *     </ul>
 * </p>
 *
 * @author Daniel Danis
 * @see SquirlsProperties
 */
@Configuration
@EnableConfigurationProperties({
        SquirlsPropertiesImpl.class,
        ClassifierPropertiesImpl.class,
        AnnotatorPropertiesImpl.class,
        DatasourcePropertiesImpl.class})
public class SquirlsAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsAutoConfiguration.class);

    private static final Properties properties = readProperties();

    private static final String SQUIRLS_VERSION = properties.getProperty("squirls.version", "unknown version");

    @Bean
    @ConditionalOnMissingBean(name = "squirlsDataDirectory")
    public Path squirlsDataDirectory(SquirlsProperties properties) throws UndefinedSquirlsResourceException {
        String dataDir = properties.getDataDirectory();
        if (dataDir == null || dataDir.isEmpty()) {
            throw new UndefinedSquirlsResourceException("Path to Squirls data directory (`--squirls.data-directory`) is not specified");
        }
        Path dataDirPath = Paths.get(dataDir);
        if (!Files.isDirectory(dataDirPath)) {
            throw new UndefinedSquirlsResourceException(String.format("Path to Squirls data directory '%s' does not point to real directory", dataDirPath));
        }
        if (LOGGER.isInfoEnabled()) LOGGER.info("Spooling up Squirls v{} using resources in `{}`", SQUIRLS_VERSION, dataDirPath.toAbsolutePath());
        return dataDirPath;
    }


    @Bean
    @ConditionalOnMissingBean(name = "squirlsGenomeAssembly")
    public String squirlsGenomeAssembly(SquirlsProperties properties) throws UndefinedSquirlsResourceException {
        String assembly = properties.getGenomeAssembly();
        if (assembly == null) {
            throw new UndefinedSquirlsResourceException("Genome assembly (`--squirls.genome-assembly`) is not specified");
        }
        return assembly;
    }

    @Bean
    @ConditionalOnMissingBean(name = "squirlsDataVersion")
    public String squirlsDataVersion(SquirlsProperties properties) throws UndefinedSquirlsResourceException {
        final String dataVersion = properties.getDataVersion();
        if (dataVersion == null) {
            throw new UndefinedSquirlsResourceException("Data version (`--squirls.data-version`) is not specified");
        }
        return dataVersion;
    }

    @Bean
    public BigWigAccessor phylopBigwigAccessor(SquirlsDataResolver squirlsDataResolver) throws IOException {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Using phyloP bigwig file at `{}`", squirlsDataResolver.phylopPath());
        return new BigWigAccessor(squirlsDataResolver.phylopPath());
    }

    @Bean
    public SquirlsDataResolver squirlsDataResolver(Path squirlsDataDirectory,
                                                   String squirlsGenomeAssembly,
                                                   String squirlsDataVersion) throws MissingSquirlsResourceException {
        SquirlsResourceVersion resourceVersion = SquirlsResourceVersion.of(squirlsDataVersion, GenomicAssemblyVersion.parseValue(squirlsGenomeAssembly));
        return new SquirlsDataResolver(squirlsDataDirectory, resourceVersion);
    }


    @Bean
    public StrandedSequenceService strandedSequenceService(SquirlsDataResolver squirlsDataResolver) throws InvalidFastaFileException {
        return new FastaStrandedSequenceService(squirlsDataResolver.genomeAssemblyReportPath(),
                squirlsDataResolver.genomeFastaPath(),
                squirlsDataResolver.genomeFastaFaiPath(),
                squirlsDataResolver.genomeFastaDictPath());
    }

    @Bean
    public GenomicAssembly genomicAssembly(StrandedSequenceService strandedSequenceService) {
        return strandedSequenceService.genomicAssembly();
    }

    @Bean
    public TranscriptModelService transcriptModelService(DataSource squirlsDatasource,
                                                         GenomicAssembly genomicAssembly,
                                                         TranscriptModelServiceOptions transcriptModelServiceOptions) throws SquirlsResourceException {
        return TranscriptModelServiceDb.of(squirlsDatasource, genomicAssembly, transcriptModelServiceOptions);
    }

    @Bean
    public TranscriptModelServiceOptions transcriptModelServiceOptions(SquirlsProperties properties) {
        int maxTxSupportLevel = properties.getDatasource().maxTranscriptSupportLevel();
        if (LOGGER.isInfoEnabled())
            LOGGER.info("Using transcripts with transcript support level <={}", maxTxSupportLevel);
        return TranscriptModelServiceOptions.of(maxTxSupportLevel);
    }

    @Bean
    public SquirlsDataService squirlsDataService(TranscriptModelService transcriptModelService, StrandedSequenceService strandedSequenceService) {
        return new SquirlsDataServiceImpl(strandedSequenceService, transcriptModelService);
    }

    @Bean
    public VariantSplicingEvaluator variantSplicingEvaluator(SquirlsDataService squirlsDataService,
                                                             SplicingAnnotator splicingAnnotator,
                                                             SquirlsClassifier squirlsClassifier) {
        return VariantSplicingEvaluator.of(squirlsDataService, splicingAnnotator, squirlsClassifier);
    }

    @Bean
    public SquirlsClassifier squirlsClassifier(ClassifierFactory classifierFactory,
                                               SquirlsProperties properties) throws UndefinedSquirlsResourceException, InvalidSquirlsResourceException {
        // TODO - all of this belongs to the classifier factory
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
            throw new InvalidSquirlsResourceException(msg);
        }
    }

    @Bean
    public ClassifierFactory classifierDataManager(@Qualifier("squirlsDatasource") DataSource squirlsDatasource) {
        return new DbClassifierFactory(squirlsDatasource);
    }

    @Bean
    public SplicingAnnotator splicingAnnotator(SquirlsProperties properties,
                                               SplicingPwmData splicingPwmData,
                                               DbKMerDao dbKMerDao,
                                               BigWigAccessor phylopBigwigAccessor) throws UndefinedSquirlsResourceException {
        AnnotatorProperties annotatorProperties = properties.getAnnotator();
        String version = annotatorProperties.getVersion();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Using `{}` splicing annotator", version);
        switch (version) {
            case "dense":
                return new DenseSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap(), phylopBigwigAccessor);
            case "agez":
                return new AGEZSplicingAnnotator(splicingPwmData, dbKMerDao.getHexamerMap(), dbKMerDao.getSeptamerMap(), phylopBigwigAccessor);
            default:
                throw new UndefinedSquirlsResourceException(String.format("invalid 'squirls.annotator.version' property value: `%s`", version));
        }
    }

    @Bean
    public DbKMerDao dbKMerDao(@Qualifier("squirlsDatasource") DataSource squirlsDatasource) {
        return new DbKMerDao(squirlsDatasource);
    }

    @Bean
    public SplicingPwmData splicingPwmData(DataSource squirlsDatasource) throws InvalidSquirlsResourceException {
        try {
            return new DbSplicingPositionalWeightMatrixParser(squirlsDatasource).getSplicingPwmData();
        } catch (CorruptedPwmException e) {
            throw new InvalidSquirlsResourceException(e);
        }
    }

    @Bean
    public DataSource squirlsDatasource(SquirlsDataResolver squirlsDataResolver) {
        Path datasourcePath = squirlsDataResolver.dataSourcePath();
        String jdbcUrl = String.format("jdbc:h2:file:%s;ACCESS_MODE_DATA=r", datasourcePath.toFile().getAbsolutePath());
        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setPoolName("squirls-pool");

        return new HikariDataSource(config);
    }

    private static Properties readProperties() {
        Properties properties = new Properties();

        try (InputStream is = SquirlsAutoConfiguration.class.getResourceAsStream("/squirls.properties")) {
            properties.load(is);
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) LOGGER.warn("Error loading properties: {}", e.getMessage());
        }
        return properties;
    }
}
