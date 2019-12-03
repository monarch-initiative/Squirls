# 3S
Code for splicing calculations.

## Build from source

Install as a Maven project:
```bash
mvn clean install
```

This command will install project files into your local Maven repository.

## Create databases for Exomiser (optional) 

The module `threes-ingest` is responsible for building of a database(s) and for running the Exomiser with 3S.

---

**Please note that building of the databases is optional**. You can download the pre-built files from AWS:
- [1902_hg19](https://exomiser-threes.s3.amazonaws.com/1902_hg19.zip)
- [1902_hg38](https://exomiser-threes.s3.amazonaws.com/1902_hg38.zip)

After unzipping, place the files into your Exomiser data directory. 

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

```
# Path to directory with 3S databases & genome FASTA file
threes.data-directory=
# genome assembly - choose from {hg19, hg38}
threes.genome-assembly=
# Exomiser-like data version
threes.data-version=
# jannovar transcript source - choose from {ucsc, refseq, ensembl}
threes.transcript-source=


## Uncomment and replace the default values if required

# max distance upstream from exon for variant to be analyzed
#threes.max-distance-exon-upstream=50

# max distance downstream from exon for variant to be analyzed
#threes.max-distance-exon-downstream=50
``` 
