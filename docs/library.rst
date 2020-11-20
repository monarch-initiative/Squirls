.. _rstlibrary:

========================
Use Squirls as a library
========================

Squirls has been designed to be used as a library within a larger frameworks for prioritization of genome variants. This
document explains how to use Squirls *API* classes to enable including splicing deleteriousness predictions into a Java
software.

Spring Boot application
~~~~~~~~~~~~~~~~~~~~~~~

When the dependency::

  <dependency>
    <groupId>org.monarchinitiative.squirls</groupId>
    <artifactId>squirls-spring-boot-starter</artifactId>
    <version>1.0.0</version>
  </dependency>

is added into the ``pom.xml`` file, then the ``VariantSplicingEvaluator`` bean is automatically configured by Spring,
provided that the environment properties

- ``squirls.data-directory``
- ``squirls.genome-assembly``
- ``squirls.data-version``

are set to valid values.

The ``VariantSplicingEvaluator`` provides methods for calculating ``SquirlsResult`` from variant coordinates described
using Java primitives:

- ``SquirlsResult evaluate(String contig, int pos, String ref, String alt)``
- ``SquirlsResult evaluate(String contig, int pos, String ref, String alt, Set<String> txIds)``

The first method calculates the results *wrt.* all transcripts that overlap the variant site, the second method narrows
the calculation down to provided transcript accession IDs (e.g. ``NM_000111.2``).

Please see the corresponding *Javadocs* to learn more about ``VariantSplicingEvaluator``, ``SquirlsResult``, etc.
