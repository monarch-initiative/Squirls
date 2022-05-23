package org.monarchinitiative.squirls.cli.cmd.setup;

import org.monarchinitiative.squirls.cli.Main;
import org.monarchinitiative.squirls.io.download.UrlResourceDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.UnaryOperator;

@CommandLine.Command(name = "ref-genome",
        header = "Download and preprocess reference genome",
        mixinStandardHelpOptions = true,
        version = Main.VERSION,
        usageHelpWidth = Main.WIDTH,
        footer = Main.FOOTER)
public class PreprocessReferenceGenomeCommand implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreprocessReferenceGenomeCommand.class);


    @CommandLine.Option(names = {"-d", "--data-directory"},
            paramLabel = "path/to/datadir",
            required = true,
            description = "Path to Squirls data directory")
    public Path dataDirectory;

    @CommandLine.Option(names = {"-g", "--genome-assembly"},
            required = true,
            description = "Genome assembly URL")
    public URL genomeUrl;

    @CommandLine.Option(names = {"-a", "--assembly-report"},
            required = true,
            description = "Assembly report URL")
    public URL assemblyReportUrl;

    @CommandLine.Option(names = {"--overwrite"},
            description = "Overwrite the genome files (default: ${DEFAULT-VALUE})")
    public boolean overwrite = false;

    @Override
    public Integer call() {
        if (!Files.isDirectory(dataDirectory)) {
            LOGGER.error("`-d | --data-directory` option must point to an existing directory");
            return 1;
        }

        try {
            // First, reference genome
            LOGGER.info("Downloading reference genome ZIP");
            downloadReferenceGenome(dataDirectory, genomeUrl, overwrite);

            // Then, assembly report.
            downloadAssemblyReport(dataDirectory, assemblyReportUrl, overwrite);
            return 0;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Download, decompress, and concatenate contigs into a single FASTA file. Then, index the FASTA file.
     *
     * @param buildDir  path to directory where Squirls data files will be created
     * @param genomeUrl url pointing to reference genome FASTA file to be downloaded
     * @param overwrite overwrite existing FASTA file if true
     */
    private static void downloadReferenceGenome(Path buildDir, URL genomeUrl, boolean overwrite) {
        Path genomeFastaPath = buildDir.resolve("genome.fa");
        GenomeAssemblyDownloader downloader = new GenomeAssemblyDownloader(genomeUrl, genomeFastaPath, overwrite);
        downloader.run(); // !
    }

    private static void downloadAssemblyReport(Path dataDirectory, URL assemblyReportUrl, boolean overwrite) throws IOException {
        Path temporary = dataDirectory.resolve("assembly_report.tmp.txt");
        UrlResourceDownloader downloader = new UrlResourceDownloader(assemblyReportUrl, temporary, overwrite);
        downloader.run(); // !

        Path destination = dataDirectory.resolve("assembly_report.txt");
        fixHg19MitochondrialLine(temporary, destination);
        Files.deleteIfExists(temporary);
    }

    private static void fixHg19MitochondrialLine(Path source, Path destination) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(source);
             BufferedWriter writer = Files.newBufferedWriter(destination)) {
             reader.lines()
                     .map(fixIfNecessary())
                     .forEachOrdered(line -> {
                         try {
                             writer.write(line);
                             writer.newLine();
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     });
        }
    }

    private static UnaryOperator<String> fixIfNecessary() {
        return line -> {
            if (line.equals("MT\tassembled-molecule\tMT\tMitochondrion\tJ01415.2\t=\tNC_012920.1\tnon-nuclear\t16569\tchrM")) {
                LOGGER.info("Fixing MT contig length (16569 -> 16571)");
                return "MT\tassembled-molecule\tMT\tMitochondrion\tJ01415.2\t=\tNC_012920.1\tnon-nuclear\t16571\tchrM";
            }
            return line;
        };
    }


}
