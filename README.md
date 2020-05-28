# 3S
Code for splicing calculations.

## Build JAR files from sources

After cloning this repository, run the following commands to build the JAR files.
```bash
cd 3S
mvn clean package
```

This command will compile & test Java code, and package classes as well as resource files into JAR files. The JAR files 
are located in the build directory, which is `<module>/target` by default (e.g. `threes-cli/target/threes-cli-1.3.0.jar`.

After successful packaging, you should be able to see the CLI help message by running:

```bash
java -jar threes-cli/target/threes-cli-1.3.0-SNAPSHOT.jar --help
```

## Command-line interface

The command-line interface defines 2 command groups:
- `generate-config` - command for generating a config file for commands from the `run` group
- `run` - commands that do useful things, e.g. annotate a single variant, a VCF file, etc. 

### `generate-config` (installation)

In order to be able to do anything useful with this app, you must download/prepare resources. Run the following to generate the config file:

```bash
java -jar threes-cli/target/threes-cli-1.3.0-SNAPSHOT.jar generate-config config.yml
``` 

The command generates an empty configuration file in YAML format. Open the file and provide paths to required resources:
- `data-directory` - path to directory with 3S databases & genome FASTA file, either downloaded or built by `threes-ingest` module. Download ZIP files with pre-built databases from AWS:
  - [2003_hg19]()
  - [2003_hg38]()
  - **TODO - add links once ready**
- `genome-assembly` - genome assembly - choose from {`hg19`, `hg38`}   
- `data-version` - Exomiser-like data version (e.g. `1902`)
- `phylop-bigwig-path` - path to bigwig file with genome-wide PhyloP scores (download the file `` from [here]())). The files for the supported genome assemblies can be downloaded at the following locations:
  - [hg19](https://hgdownload.cse.ucsc.edu/goldenpath/hg19/phyloP100way/hg19.100way.phyloP100way.bw)
  - [hg38](https://hgdownload.soe.ucsc.edu/goldenPath/hg38/phyloP100way/hg38.phyloP100way.bw)  

### Annotate VCF file

**TODO - write the docs**

## Build database files for supported genome assemblies

**TODO - write the docs**  

# Everything below this line is not up-to-date anymore

### Download required data

The build process requires presence of following files in the `build-dir`:
- pre-built Jannovar caches with `v0.29` 
- YAML file with positional-weight matrices for canonical splice donor & acceptor sites

Let's say the `build-dir` points to `/home/user/data` (`--build-dir=/home/user/data`). You need to download & unpack content of the [threes-data.zip](https://exomiser-threes.s3.amazonaws.com/threes-data.zip) into the `/home/user/data`. In result, there will be 2 directories in the  `build-dir`:
- `/home/user/data/jannovar`
- `/home/user/data/pwm` 


### Build the database(s)
After downloading & unpacking the above files, we can start building the databases. We need to build databases for each transcript source:
- *refseq*,
- *ucsc*, and
- *ensembl*

The command below will create a new directory `/path/to/build/directory/1902_hg19` and store all the files inside. At first it will download & process the UCSC reference genome, then the splicing database with *RefSeq* data will be built. Edit the `--jannovar-transcript-source` argument if you want to build splicing database for other transcript source.

```bash
java -jar threes-ingest-1.0.4.jar
--build-dir=/home/user/data --version=1902 --genome-assembly=hg19 --jannovar-transcript-source=refseq
```
> *Note:*
> `--version` - use any string you want, this is present in order to follow Exomiser build process as closely as possible
> `--genome-assembly` - choose from `hg19`, and `hg38`
> `--jannovar-transcript-source` - choose from `refseq`, `ucsc`, and `ensembl`

More info regarding building of the database can be found in the *README.md* file of the `threes-ingest` module

### Join database & FASTA file with Exomiser data
The previous command created directory with FASTA file and splicing databases (e.g. `1902_hg19`). Copy all the files from this directory into your Exomiser data directory.


## Use 3S code as a library

Use following dependency after installing the project into your local Maven repository:

```xml
<dependency>
    <groupId>org.monarchinitiative.threes</groupId>
    <artifactId>threes-core</artifactId>
    <version>1.0.4</version>
</dependency>
```

## Use 3S code as a library with autoconfiguration

Use following starter within your Spring Boot project in order to use the 3S code as a library:

```xml
<dependency>
    <groupId>org.monarchinitiative.threes</groupId>
    <artifactId>threes-spring-boot-starter</artifactId>
    <version>1.0.4</version>
</dependency>
```

When using the starter, you have to provide path to 3S data directory, and some other properties as well:
