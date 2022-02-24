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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.monarchinitiative.squirls.core.SquirlsException;
import org.monarchinitiative.squirls.ingest.data.ZipCompressionWrapper;
import org.monarchinitiative.squirls.io.SquirlsClassifierVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Daniel Danis
 */
@CommandLine.Command(name = "ingest",
        aliases = {"I"},
        header = "Ingest resources into Squirls database",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
public class IngestCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestCommand.class);

    @CommandLine.Option(names = {"-c", "--config"},
            required = true,
            paramLabel = "squirls-config.yml",
            description = "Path to configuration file generated by the `generate-config` command")
    public Path configFile;

    @CommandLine.Option(names = {"-b", "--build-dir"},
            required = true,
            paramLabel = "/path/to/build/dir",
            description = "Path to directory where the database will be built")
    public Path buildDirPath;

    @CommandLine.Option(names = {"-d", "--db-version"},
            required = true,
            paramLabel = "2102",
            description = "Database version, e.g. `2102` for database built in Feb 2021")
    public String version;

    @CommandLine.Option(names = {"-a", "--assembly"},
            required = true,
            paramLabel = "{hg19, hg38}",
            description = "Genomic assembly version")
    public String assembly;

    private static String normalizeAssemblyString(String assembly) throws SquirlsException {
        switch (assembly.toLowerCase()) {
            case "hg19":
            case "grch37":
                return "hg19";
            case "hg38":
            case "grch38":
                return "hg38";
            default:
                throw new SquirlsException(String.format("Unknown assembly string '%s'", assembly));
        }
    }

    private static String getVersionedAssembly(String assembly, String version) throws SquirlsException {
        assembly = normalizeAssemblyString(assembly);
        // a string like `1902_hg19`
        return version + "_" + assembly;
    }

    private static IngestProperties readIngestProperties(Path configFile) throws IOException {
        Yaml yaml = new Yaml(new Constructor(IngestProperties.class));
        try (InputStream is = Files.newInputStream(configFile)) {
            return yaml.load(is);
        }
    }

    @Override
    public Integer call() {
        LOGGER.info("Running the `ingest` command");
        try {
            // 0 - parse command line
            if (!buildDirPath.toFile().isDirectory()) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Not a directory: {}", buildDirPath);
                return 1;
            }
            if (!buildDirPath.toFile().canWrite()) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("Directory not writable: {}", buildDirPath);
                return 1;
            }

            LOGGER.info("Build directory: `{}`", buildDirPath);
            LOGGER.info("Using version `{}` and genome assembly `{}`", version, assembly);

            IngestProperties ingestProperties = readIngestProperties(configFile);


            // 1 - create build folder
            URL genomeUrl = new URL(ingestProperties.fastaUrl());
            URL assemblyReportUrl = new URL(ingestProperties.assemblyReportUrl());
            URL phylopUrl = new URL(ingestProperties.phylopUrl());
            URL refseqGtfUrl = new URL(ingestProperties.refseqGtfUrl());
            URL gencodeGtfUrl = new URL(ingestProperties.gencodeGtfUrl());

            String versionedAssembly = getVersionedAssembly(assembly, version);
            Path versionedAssemblyBuildPath = buildDirPath.resolve(versionedAssembly);
            Path genomeBuildDir = Files.createDirectories(versionedAssemblyBuildPath);
            LOGGER.info("Building resources in `{}`", versionedAssemblyBuildPath);


            // 2 - read classifier data
            Map<SquirlsClassifierVersion, Path> classifiers = ingestProperties.classifiers().stream()
                    .collect(Collectors.toMap(
                            clfData -> SquirlsClassifierVersion.parseString(clfData.version()),
                            clfData -> Paths.get(clfData.classifierPath())));


            // 3 - build database
            SquirlsDataBuilder.buildDatabase(genomeBuildDir,
                    genomeUrl, assemblyReportUrl, refseqGtfUrl, gencodeGtfUrl, phylopUrl,
                    Path.of(ingestProperties.splicingInformationContentMatrix()),
                    Path.of(ingestProperties.hexamerTsvPath()),
                    Path.of(ingestProperties.septamerTsvPath()),
                    classifiers, versionedAssembly);


            // 4 - calculate SHA256 digest for the resource files
            List<File> resources = Arrays.stream(Objects.requireNonNull(genomeBuildDir.toFile().listFiles())).collect(Collectors.toList());
            LOGGER.info("Calculating SHA256 digest for resource files in `{}`", genomeBuildDir);
            Map<File, String> fileToDigest = new HashMap<>();
            DigestUtils digest = new DigestUtils(MessageDigestAlgorithms.SHA_256);

            // calculate digests
            for (File resource : resources) {
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Calculating SHA256 digest for `{}`", resource);
                String hexDigest = digest.digestAsHex(resource);
                fileToDigest.put(resource, hexDigest);
            }

            // write digests to a file
            Path digestFilePath = genomeBuildDir.resolve(versionedAssembly + ".sha256");
            LOGGER.info("Storing the digest into `{}`", digestFilePath);
            try (BufferedWriter digestWriter = Files.newBufferedWriter(digestFilePath)) {
                for (File resource : fileToDigest.keySet()) {
                    String line = String.format("%s  %s", fileToDigest.get(resource), resource.getName());
                    digestWriter.write(line);
                    digestWriter.write(System.lineSeparator());
                }
            }

            // 5 - compress all the files into a single ZIP file
            resources = Arrays.stream(Objects.requireNonNull(genomeBuildDir.toFile().listFiles())).collect(Collectors.toList());
            Path zipPath = buildDirPath.resolve(versionedAssembly + ".zip");
            LOGGER.info("Compressing the resource files into a single ZIP file `{}`", zipPath);
            try (ZipCompressionWrapper wrapper = new ZipCompressionWrapper(zipPath.toFile())) {
                for (File resource : resources) {
                    LOGGER.info("Compressing `{}`", resource);
                    wrapper.addResource(resource, resource.getName());
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
            return 1;
        }
        return 0;
    }
}

