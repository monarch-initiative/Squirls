# 3S ingest

This module is responsible for building of 3S databases for *refseq*, *ensembl*, and *ucsc* transcripts.

## How to build database

The command below will create a new directory `/home/user/data/1902_hg19` and store all the files inside. 
At first it will download & process the UCSC reference genome, then the splicing database with *RefSeq* data will be built. Edit the `--jannovar-transcript-source` argument if you want to build splicing database for another transcript source.

```bash
java -jar threes-ingest-1.0.4.jar
--build-dir=/home/user/data --version=1902 --genome-assembly=hg19 --jannovar-transcript-source=refseq
```
> **Note:**
> - `--version` - use any string you want, this is present in order to follow Exomiser build process as closely as possible
> - `--genome-assembly` - choose from `hg19`, and `hg38`
> - `--jannovar-transcript-source` - choose from `refseq`, `ucsc`, and `ensembl`

## How to use the database building process within another software

The methods for database building are defined in the `org.monarchinitiative.threes.ingest.ThreesDataBuilder` class. 

An example usage of these methods is showed in the `org.monarchinitiative.threes.ingest.Main` class.
