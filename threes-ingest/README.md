# 3S ingest

This module is responsible for building of 3S databases for *Refseq*, *Ensembl*, and *Ucsc* transcripts.

## How to build database

The command below will create a new directory `/home/user/data/1902_hg19` and store all the files inside. 
At first it will download & process the UCSC reference genome, then the splicing database with *RefSeq*, *Ucsc*, and *Ensembl* transcripts will be built in case appropriate Jannovar caches are present in the `/path/to/jannovar/dir` folder. 

```bash
java -jar threes-ingest-1.0.4.jar
--build-dir=/home/user/data --genome-assembly=hg19 --version=1902 --jannovar-transcript-db-dir=/path/to/jannovar/dir
```
> **Note:**
> - `--version` - use any string you want, this is present in order to follow Exomiser build process as closely as possible
> - `--genome-assembly` - choose from `hg19`, and `hg38`

## How to use the database building process within another software

The methods for database building are defined in the `org.monarchinitiative.threes.ingest.ThreesDataBuilder` class. 

An example usage of these methods is showed in the `org.monarchinitiative.threes.ingest.Main` class.
