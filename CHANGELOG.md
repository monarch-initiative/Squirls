# Changelog

## ✈ the latest
- `squirls-cli`
  - allow to block-compress (`bgzip`) the annotated VCF on the fly by using `-f vcfgz` option
  - do not fail due to an exception being thrown by Jannovar when annotating a VCF
- `squirls-ingest`
  - prevent ingesting transcripts with duplicate *ENST* id 

## ✈ v1.0.0-RC4

- `Squirls`
  - make the code repository public, enable *Read the Docs* integration
  - break into smaller modules
- `squirls-cli`
  - finalize the HTML report and SVG graphics layout
  - allow writing results in multiple result formats, including VCF, TSV/CSV 
  - report positions of candidate cryptic variants
  - add distribution zip
- `squirls-core`
  - use `apiguardian-api` to label the API elements
  - use `svart v1.0.0` to represent variants, genomic regions, and the other genomic coordinates
  - remove external dependencies such as `JBlas`, 
- `squirls-ingest`
  - download genomic assembly report
  - calculate SHA256 checksums
  - replace `argparse4j` by `picocli`
- `squirls-io`
  - create the module for handling creation of Squirls structures
- `squirls-spring-boot-autoconfigure`
  - fix bug involving database initialization in HPC environments

## ✈ v1.0.0-RC3

- `Squirls`
  - improve documentation
  - use Jannovar v0.35, SLF4J v1.7.30
- `squirls-cli`
  - use Picocli for CLI definition, prettify CLI
  - support HTML and VCF output formats
  - numerous bug fixes
  - allow specifying # threads
  - improve progress reporting
- `squirls-core`
  - mark API classes
- `squirls-ingest`
  - create a ZIP file after ingest
  - add sanity checks for transcripts

## ✈ v1.0.0-RC2

- `squirls-cli`
  - implement commands `annotate-csv`, `annotate-vcf`, and `annotate-pos`
  - prettify the HTML report produced by `analyze-vcf` command
- `squirls-core`
  - performance improvements, increase test coverage
  - improve handling of positions with missing conservation scores

## ✈ v1.0.0-RC1

- `squirls-core`
  - implement the final prediction model
  - include AGEZ features
  - use full logistic regression (not *max*-dependent)
  - simplify dependencies
- `squirls-ingest`
  - optimize the database building process
  - include PhyloP bigwig file into data directory
- `squirls-spring-boot-autoconfigure`
  - improve error reporting
  - include PhyloP into autoconfiguration

---
**The versions below were used in development only, and they are not available anymore. Please see `VERSIONING.md` to
see the details regarding the new versioning scheme**

## v1.3.0

✈ re-brand to Squirls, restructure code

✈ implement prototype SVG graphics generation using VMVT

✈ add splicing features

## v1.2.0

✈ rename API class `SplicingEvaluator` to `SplicingAnnotator`

✈ implement `VariantSplicingEvaluator` - high level API interface

✈ rework autoconfiguration:

- add configuration properties as `ThreesProperties` class,
- create `@EnableThrees` annotation

✈ add `annotate-pos` command to CLI

## v1.1.0

✈ store all transcripts in a single database table

- allow query by coordinates and accession ID
- use internal reference dictionary to perform mapping from contig name into contig id
- refactor database building process (ingest)

✈ add modules (Java 11)

✈ add *dense* splicing evaluator (WIP)

## v1.0.4

✈ externalize max allowed #bp upstream and downstream from an exon in order to allow an intronic variant to be analyzed

✈ allow selecting which scoring strategies to use

✈ add this changelog ♫♪♬

## v1.0.3

✈ add septamers scorer

✈ fix bugs

## v1.0.2

✈ fix bugs

## v1.0.1

✈ add `threes-spring-boot-starter` & autoconfigure

✈ update Spring version

✈ fix bugs

## v1.0.0

✈ the first release with prototype functionality