# Squirls Ingest

This module is responsible for building of Squirls resource files.

## How to build the resource files

There are two commands that need to be run in order to build the resource files for a genome assembly:

- `generate-config` - generate the config file
- `ingest | run-ingest` - build the resource directory

### Generate a config file

We need to generate and fill the config file first. The config file requires paths/URLs to public resources 
and the resources generated in Squirls project that are hosted on Zenodo.

```bash
java -jar squirls-ingest.jar generate-config config.yml
``` 

Then, open the `config.yml` file and provide the required information.

### Build the resource

Having the config file ready, we can build the resource directory.

```bash
java -jar squirls-ingest.jar ingest -c config.yml --assembly hg19 --db-version 2005 --build-dir path/to/build-dir 
```

where

- `path/to/build-dir` - denotes a path to resource directory
- `2005` - denotes an arbitrary version tag for this build
- `hg19` - denotes a genome assembly tag

After running the command above, the `build-dir` should have a similar structure:

```
build-dir
  \- 2005_hg19
    |- assembly_report.txt    
    |- genome.fa
    |- genome.fa.dict
    |- genome.fa.fai
    |- squirls.mv.db
    |- tx.ensembl.ser
    |- tx.refseq.ser
    \- tx.ucsc.ser
``` 

## How to build the resource files within another software

The methods for resource building are defined in the `org.monarchinitiative.squirls.ingest.SquirlsDataBuilder` class.

An example usage of these methods is showed in the `org.monarchinitiative.squirls.ingest.cmd.RunIngestCommand` class.
