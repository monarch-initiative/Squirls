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

package org.monarchinitiative.squirls.ingest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.monarchinitiative.squirls.core.SquirlsException;
import org.monarchinitiative.squirls.core.reference.SplicingParameters;
import org.monarchinitiative.squirls.core.reference.SplicingPwmData;
import org.monarchinitiative.squirls.ingest.data.GenomeAssemblyDownloader;
import org.monarchinitiative.squirls.ingest.data.UrlResourceDownloader;
import org.monarchinitiative.squirls.ingest.parse.FileKMerParser;
import org.monarchinitiative.squirls.ingest.parse.InputStreamBasedPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.io.SplicingPositionalWeightMatrixParser;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;
import org.monarchinitiative.squirls.io.db.DbClassifierFactory;
import org.monarchinitiative.squirls.io.db.DbKMerDao;
import org.monarchinitiative.squirls.io.db.PwmIngestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A static class with methods for building splicing database.
 *
 * @author Daniel Danis
 * @see Main for an example usage
 */
public class SquirlsDataBuilder {

    public static final String DONOR_NAME = "SPLICE_DONOR_SITE";

    public static final String ACCEPTOR_NAME = "SPLICE_ACCEPTOR_SITE";

    private static final String LOCATIONS = "classpath:db/migration";

    private static final Logger LOGGER = LoggerFactory.getLogger(SquirlsDataBuilder.class);

    private SquirlsDataBuilder() {
        // private no-op
    }

    static int applyMigrations(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(LOCATIONS)
                .load();
        return flyway.migrate();
    }

    static DataSource makeDataSource(Path databasePath) {
        String jdbcUrl = String.format("jdbc:h2:file:%s", databasePath.toString());
        HikariConfig config = new HikariConfig();
        config.setUsername("sa");
        config.setPassword("sa");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);

        return new HikariDataSource(config);
    }

    /**
     * Store given <code>donor</code>, <code>acceptor</code>, and <code>parameters</code> into <code>dataSource</code>>.
     *
     * @param dataSource      {@link DataSource} where to matrices will be stored
     * @param splicingPwmData {@link SplicingPwmData} with data representing splice sites
     */
    private static void processPwms(DataSource dataSource, SplicingPwmData splicingPwmData) {
        PwmIngestDao pwmIngestDao = new PwmIngestDao(dataSource);
        SplicingParameters parameters = splicingPwmData.getParameters();
        pwmIngestDao.insertDoubleMatrix(splicingPwmData.getDonor(), DONOR_NAME, parameters.getDonorExonic(), parameters.getDonorIntronic());
        pwmIngestDao.insertDoubleMatrix(splicingPwmData.getAcceptor(), ACCEPTOR_NAME, parameters.getAcceptorExonic(), parameters.getAcceptorIntronic());
    }

    /**
     * Download, decompress, and concatenate contigs into a single FASTA file. Then, index the FASTA file.
     *
     * @param genomeUrl         url pointing to reference genome FASTA file to be downloaded
     * @param buildDir          path to directory where Squirls data files will be created
     * @param versionedAssembly a string like `1710_hg19`, etc.
     * @param overwrite         overwrite existing FASTA file if true
     */
    static Runnable downloadReferenceGenome(URL genomeUrl, Path buildDir, String versionedAssembly, boolean overwrite) {
        Path genomeFastaPath = buildDir.resolve(String.format("%s.fa", versionedAssembly));
        return new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, overwrite);
    }

    /**
     * Store data for hexamer and septamer-dependent methods.
     *
     * @param dataSource  data source for a database
     * @param hexamerMap  map with hexamer scores
     * @param septamerMap map with septamer scores
     */
    private static void processKmers(DataSource dataSource, Map<String, Double> hexamerMap, Map<String, Double> septamerMap) {
        DbKMerDao dao = new DbKMerDao(dataSource);
        int updated = dao.insertHexamers(hexamerMap);
        LOGGER.info("Updated {} rows in hexamer table", updated);
        updated = dao.insertSeptamers(septamerMap);
        LOGGER.info("Updated {} rows in septamer table", updated);
    }

    /**
     * Serialize data required to construct {@link org.monarchinitiative.squirls.core.VariantSplicingEvaluator}
     *
     * @param dataSource data source for a database
     * @param clfVersion classifier version
     * @param clfBytes   all data required to construct {@link org.monarchinitiative.squirls.core.VariantSplicingEvaluator}
     */
    private static void processClassifier(DataSource dataSource, SquirlsClassifierVersion clfVersion, byte[] clfBytes) {
        DbClassifierFactory factory = new DbClassifierFactory(dataSource);

        // squirls classifier
        LOGGER.info("Inserting classifier `{}`", clfVersion);
        int updated = factory.storeClassifier(clfVersion, clfBytes);

        LOGGER.info("Updated {} rows", updated);
    }

    private static Map<SquirlsClassifierVersion, byte[]> readClassifiers(Map<SquirlsClassifierVersion, Path> clfs) throws IOException {
        Map<SquirlsClassifierVersion, byte[]> classifiers = new HashMap<>();
        for (Map.Entry<SquirlsClassifierVersion, Path> entry : clfs.entrySet()) {
            LOGGER.info("Reading classifier `{}` from `{}`", entry.getKey(), entry.getValue());
            try (InputStream is = Files.newInputStream(entry.getValue())) {
                classifiers.put(entry.getKey(), is.readAllBytes());
            }
        }
        return classifiers;
    }

    /**
     * Build the database given inputs.
     *
     * @param buildDir          path to directory where the database file should be stored
     * @param genomeUrl         URL pointing to `tar.gz` file with reference genome
     * @param refseqUrl         URL pointing to Jannovar RefSeq transcript database
     * @param ensemblUrl        URL pointing to Jannovar Ensembl transcript database
     * @param ucscUrl           URL pointing to Jannovar UCSC transcript database
     * @param yamlPath          path to file with splice site definitions
     * @param versionedAssembly a string like `1710_hg19`, etc.
     * @throws SquirlsException if anything goes wrong
     */
    public static void buildDatabase(Path buildDir, URL genomeUrl, URL genomeAssemblyReport,
                                     URL refseqUrl, URL ensemblUrl, URL ucscUrl,
                                     URL phylopUrl,
                                     Path yamlPath,
                                     Path hexamerPath, Path septamerPath,
                                     Map<SquirlsClassifierVersion, Path> classifiers,
                                     String versionedAssembly) throws SquirlsException {

        // 0 - initiate download of reference genome FASTA file & PhyloP bigwig file
        Path genomeAssemblyReportPath = buildDir.resolve(String.format("%s.assembly_report.txt", versionedAssembly));
        Path phyloPPath = buildDir.resolve(String.format("%s.phylop.bw", versionedAssembly));
        Path refseqPath = buildDir.resolve(String.format("%s.tx.refseq.ser", versionedAssembly));
        Path ensemblPath = buildDir.resolve(String.format("%s.tx.ensembl.ser", versionedAssembly));
        Path ucscPath = buildDir.resolve(String.format("%s.tx.ucsc.ser", versionedAssembly));

        ExecutorService es = Executors.newFixedThreadPool(3);
        es.submit(downloadReferenceGenome(genomeUrl, buildDir, versionedAssembly, false));
        es.submit(new UrlResourceDownloader(phylopUrl, phyloPPath, false));
        es.submit(new UrlResourceDownloader(genomeAssemblyReport, genomeAssemblyReportPath, false));
        es.submit(new UrlResourceDownloader(refseqUrl, refseqPath, true));
        es.submit(new UrlResourceDownloader(ensemblUrl, ensemblPath, true));
        es.submit(new UrlResourceDownloader(ucscUrl, ucscPath, true));

        // 1a - parse YAML with splicing matrices
        SplicingPwmData splicingPwmData;
        try (InputStream is = Files.newInputStream(yamlPath)) {
            SplicingPositionalWeightMatrixParser parser = new InputStreamBasedPositionalWeightMatrixParser(is);
            splicingPwmData = parser.getSplicingPwmData();
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 1b - parse k-mer maps
        Map<String, Double> hexamerMap;
        Map<String, Double> septamerMap;
        try {
            hexamerMap = new FileKMerParser(hexamerPath).getKmerMap();
            septamerMap = new FileKMerParser(septamerPath).getKmerMap();
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // 2 - create and fill the database
        // 2a - initialize database
        Path databasePath = buildDir.resolve(String.format("%s.splicing", versionedAssembly));
        LOGGER.info("Creating database at `{}`", databasePath);
        DataSource dataSource = makeDataSource(databasePath);

        // 2b - apply migrations
        final int i = applyMigrations(dataSource);
        LOGGER.info("Applied {} migrations", i);

        // 2c - store PWM data
        LOGGER.info("Inserting PWMs");
        processPwms(dataSource, splicingPwmData);

        // 2d - store k-mer maps
        LOGGER.info("Inserting k-mer maps");
        processKmers(dataSource, hexamerMap, septamerMap);

        // 2e - store classifier
        try {
            LOGGER.info("Inserting classifiers");
            Map<SquirlsClassifierVersion, byte[]> clfData = readClassifiers(classifiers);
            for (Map.Entry<SquirlsClassifierVersion, byte[]> entry : clfData.entrySet()) {
                processClassifier(dataSource, entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            throw new SquirlsException(e);
        }

        // now wait until all the downloads complete
        try {
            es.shutdown();
            while (!es.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.print('.');
            }
            System.out.print('\n');
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupting the download");
            es.shutdownNow();
        }

        if (dataSource instanceof Closeable) {
            try {
                LOGGER.info("Closing database connections");
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                LOGGER.warn("Could not close the datasource");
            }
        }
    }

}
