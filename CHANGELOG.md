# Changelog

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