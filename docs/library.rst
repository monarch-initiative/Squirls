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

The ``VariantSplicingEvaluator`` provides methods:

- ``SquirlsResult evaluate(String contig, int pos, String ref, String alt)`` - calculate
  splicing scores for given variant with respect to all transcripts the variant overlaps with

- ``SquirlsResult evaluate(String contig, int pos, String ref, String alt, Set<String> txIds)`` - calculate
  splicing scores for given variant with respect to given transcript IDs ``txIds``. The variant is evaluated with
  respect to all overlapping transcripts, if ``txIds`` is empty

Please see the corresponding *Javadocs* to learn more about ``VariantSplicingEvaluator``, ``SquirlsResult``, etc.
