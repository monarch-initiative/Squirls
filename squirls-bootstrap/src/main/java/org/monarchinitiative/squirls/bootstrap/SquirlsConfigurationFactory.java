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
import org.monarchinitiative.squirls.core.SquirlsDataService;
import org.monarchinitiative.squirls.core.VariantSplicingEvaluator;
import org.monarchinitiative.squirls.core.classifier.SquirlsClassifier;
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
import org.monarchinitiative.squirls.io.db.TranscriptModelServiceDb;
import org.monarchinitiative.squirls.io.sequence.FastaStrandedSequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class prepares {@link Squirls} for given {@link SquirlsResourceVersion}.
 * <p>
 * To create the factory, you must provide path to the Squirls data directory. The data directory must have
 * the following structure:
 * <pre>
 * path/to/squirls/data_directory:
 *  |- 1902_hg19:
 *  |   |- 1902_hg19.assembly_report.txt
 *  |   |- 1902_hg19.fa
 *  |   |- 1902_hg19.fa.dict
 *  |   |- 1902_hg19.fa.fai
 *  |   |- 1902_hg19.phylop.bw
 *  |   \- 1902_hg19_splicing.mv.db
 *  \- 1902_hg38:
 *      |- 1902_hg38.fa
 *      ...
 * </pre>
 * <p>
 * In the example above, <code>path/to/squirls/data_directory</code> should be provided as the path to
 * the Squirls data directory. Then, the factory provides {@link Squirls} for
 * genomes {@link GenomicAssemblyVersion#GRCH37} and {@link GenomicAssemblyVersion#GRCH38}, version <code>1902</code>
 * (February 2019).
 *
 * <p>
 * Use {@link #supportedResourceVersions()} to get all supported resource versions, and
 * {@link #getSquirls(SquirlsResourceVersion)} to get {@link Squirls} for the particular
 * {@link SquirlsResourceVersion}.
 *
 * @since 1.0.1
 * @author Daniel Danis
 */
@API(status = API.Status.STABLE, since = "1.0.1")
public class SquirlsConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsConfigurationFactory.class);

    private static final Pattern DIR_PATTERN = Pattern.compile("(?<version>\\d{4})_(?<assembly>(hg19|hg38))");

    private final Map<SquirlsResourceVersion, SquirlsDataResolver> resolverMap;

    private final SquirlsProperties properties;

    public static SquirlsConfigurationFactory of(SquirlsProperties properties) throws MissingSquirlsResourceException, UndefinedSquirlsResourceException {
        return new SquirlsConfigurationFactory(properties);
    }

    private SquirlsConfigurationFactory(SquirlsProperties properties) throws MissingSquirlsResourceException, UndefinedSquirlsResourceException {
        this.properties = Objects.requireNonNull(properties, "Squirls properties must not be null");
        File dataDir = new File(Objects.requireNonNull(properties.getDataDirectory(), "Data directory must not be null"));
        this.resolverMap = Map.copyOf(exploreDataDirectory(dataDir));
    }

    private static Map<SquirlsResourceVersion, SquirlsDataResolver> exploreDataDirectory(File dataDirectory) throws MissingSquirlsResourceException, UndefinedSquirlsResourceException {
        File[] children = dataDirectory.listFiles();
        if (children == null)
            throw new UndefinedSquirlsResourceException("Path `" + dataDirectory.getAbsolutePath() + "` does not point to a directory");

        Map<SquirlsResourceVersion, SquirlsDataResolver> resolverMap = new HashMap<>();
        for (File child : children) {
            Matcher matcher = DIR_PATTERN.matcher(child.getName());
            if (matcher.matches()) {
                String version = matcher.group("version");
                GenomicAssemblyVersion assembly = GenomicAssemblyVersion.parseValue(matcher.group("assembly"));
                SquirlsResourceVersion resourceVersion = SquirlsResourceVersion.of(version, assembly);

                SquirlsDataResolver resolver = new SquirlsDataResolver(dataDirectory.toPath(), resourceVersion);
                resolverMap.put(resourceVersion, resolver);
            } else {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info("Ignoring folder `{}`", child.getAbsolutePath());
            }
        }

        return resolverMap;
    }

    private static Squirls configure(SquirlsDataResolver dataResolver, SquirlsProperties properties) throws IOException, SquirlsResourceException {
        DataSource squirlsDatasource = squirlsDatasource(dataResolver);

        SquirlsDataService squirlsDataService = configureSquirlsDataService(dataResolver, squirlsDatasource);

        BigWigAccessor phylopBigwigAccessor = new BigWigAccessor(dataResolver.phylopPath());
        SplicingAnnotator splicingAnnotator = configureSplicingAnnotator(properties, squirlsDatasource, phylopBigwigAccessor);

        SquirlsClassifier squirlsClassifier = configureSquirlsClassifier(squirlsDatasource, properties);
        VariantSplicingEvaluator variantSplicingEvaluator = VariantSplicingEvaluator.of(squirlsDataService, splicingAnnotator, squirlsClassifier);

        return new SquirlsImpl(dataResolver.resourceVersion(), squirlsDataService, splicingAnnotator, squirlsClassifier, variantSplicingEvaluator);
    }

    private static DataSource squirlsDatasource(SquirlsDataResolver squirlsDataResolver) {
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

    private static SquirlsDataService configureSquirlsDataService(SquirlsDataResolver dataResolver, DataSource squirlsDatasource) throws SquirlsResourceException {
        StrandedSequenceService strandedSequenceService = new FastaStrandedSequenceService(dataResolver.genomeAssemblyReportPath(),
                dataResolver.genomeFastaPath(),
                dataResolver.genomeFastaFaiPath(),
                dataResolver.genomeFastaDictPath());
        TranscriptModelService transcriptModelService = TranscriptModelServiceDb.of(squirlsDatasource, strandedSequenceService.genomicAssembly());
        return new SquirlsDataServiceImpl(strandedSequenceService, transcriptModelService);
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

    private static SquirlsClassifier configureSquirlsClassifier(DataSource squirlsDatasource,
                                                                SquirlsProperties properties) throws SquirlsResourceException {
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

    /**
     * @return set of {@link SquirlsResourceVersion}s available from this factory
     */
    public Set<SquirlsResourceVersion> supportedResourceVersions() {
        return resolverMap.keySet();
    }

    /**
     * @param resourceVersion resource version
     * @return configuration with
     * @throws SquirlsResourceException in case there are any issues with initialization (e.g. missing files)
     */
    public Squirls getSquirls(SquirlsResourceVersion resourceVersion) throws SquirlsResourceException {
        if (!resolverMap.containsKey(resourceVersion)) {
            throw new SquirlsResourceException("Resource " +
                    resourceVersion.version() + '_' + resourceVersion.assembly().version() +
                    "` is not present in `" + properties.getDataDirectory() + '`');
        }
        SquirlsDataResolver resolver = resolverMap.get(resourceVersion);

        try {
            return configure(resolver, properties);
        } catch (IOException e) {
            throw new SquirlsResourceException(e);
        }
    }

}
