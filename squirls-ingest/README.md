# Squirls Ingest

This module is responsible for building of Squirls resource files.

## How to build the resource files

There are two commands that need to be run in order to build the resource files for a genome assembly:

- `generate-config` - generate the config file
- `ingest | run-ingest` - build the resource directory

### Generate a config file

We need to generate and fill the config file first. If you do not have some resources, download a resource bundle from
[here](https://exomiser-threes.s3.amazonaws.com/threes-build-resources.zip).

```bash
java -jar squirls-ingest-1.0.4.jar generate-config config.yml
``` 

Then, open the `config.yml` file and provide the required information.

### Build the resource

Having the config file ready, we can build the resource directory.

```bash
java -jar squirls-ingest-1.0.4.jar ingest -c config.yml run-ingest build-dir 2005 hg19
```

where

- `build-dir` - denotes a path to resource directory
- `2005` - denotes an arbitrary version tag for this build
- `hg19` - denotes a genome assembly tag

After running the command above, the `build-dir` should have a similar structure:

```
build-dir
  \- 2005_hg19
    |- 2005_hg19.fa
    |- 2005_hg19.fa.dict
    |- 2005_hg19.fa.fai
    \- 2005_hg19_splicing.mv.db
``` 

## How to build the resource files within another software

The methods for resource building are defined in the `org.monarchinitiative.squirls.ingest.SquirlsDataBuilder` class.

An example usage of these methods is showed in the `org.monarchinitiative.squirls.ingest.cmd.RunIngestCommand` class.
