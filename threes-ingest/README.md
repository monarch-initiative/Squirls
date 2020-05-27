# 3S ingest

This module is responsible for building of 3S resource files for *Refseq*, *Ensembl*, and *Ucsc* transcripts.

## How to build the resource files

### Generate a config file
We need to generate and fill the config file first. If you do not have some resources, download a resource bundle from 
[here](https://exomiser-threes.s3.amazonaws.com/threes-build-resources.zip).

```bash
java -jar threes-ingest-1.0.4.jar generate-config config.yml
``` 

Then, open the `config.yml` file and provide the required information. 

### Build the resource
Having config file ready, we can build the resource directory:

```bash
java -jar threes-ingest-1.0.4.jar ingest -c config.yml run-ingest build-dir 2005 hg19
```
where
- `build-dir` - denotes a path to resource directory
- `2005` - denotes an arbitrary version tag for this build
- `hg19` - denotes a genome assembly tag 

After 

## How to build the resource files within another software

The methods for resource building are defined in the `org.monarchinitiative.threes.ingest.ThreesDataBuilder` class. 

An example usage of these methods is showed in the `org.monarchinitiative.threes.ingest.cmd.RunIngestCommand` class.
