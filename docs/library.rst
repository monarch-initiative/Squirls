.. _rstlibrary:

========================
Use Squirls as a library
========================

Squirls is implemented as a modular Java application to allow to be used both as a standalone application and as a library.
This document explains how to use Squirls as a component/library, to predict deleteriousness of variants on splicing
within a larger application for analysis of genome variants.

The following sections describe how to use Squirls as a module in other Java tool.

Install Squirls modules into your local Maven repository
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

As the first step, Squirls needs to be installed into the local Maven repository.
The installation requires JDK 11 or better to be present in the environment. Squirls uses the amazing
`Maven Wrapper <https://github.com/takari/maven-wrapper>`_ to build the project::

  git clone https://github.com/TheJacksonLaboratory/Squirls
  cd Squirls
  ./mvnw install

After the successful build, Squirls artifacts are installed in your local Maven repository and, therefore, available for
using as dependencies of other projects.

Bootstrap Squirls
~~~~~~~~~~~~~~~~~

Squirls can be easily used as a module within a larger Java application. To add Squirls into your codebase, first include
the ``squirls-bootstrap`` as a dependency, e.g. by adding the following into the ``pom.xml`` of your Maven project::

  <dependency>
    <groupId>org.monarchinitiative.squirls</groupId>
    <artifactId>squirls-bootstrap</artifactId>
    <version>${project.version}</version>
  </dependency>

.. note::
  Replace ``${project.version}`` placeholder with an actual Squirls release, i.e. ``1.0.1``.

The programmatic initialization of Squirls is very straightforward.
To kick the tires, get ready to provide:

* ``squirls.data-directory``,
* ``squirls.genome-assembly``, and
* ``squirls.data-version``,

as described in the :ref:`mandatory-parameters-ref` section.

First, create ``SquirlsProperties`` using the builder, and instantiate ``SquirlsConfigurationFactory``::

  File dataDirectory = ... ; // path to Squirls data directory, i.e. `/project/joe/squirls_resources`
  SquirlsProperties squirlsProperties = SimpleSquirlsProperties.builder(dataDirectory).build();
  SquirlsConfigurationFactory squirlsFactory = SquirlsConfigurationFactory.of(squirlsProperties);

Next, get ``Squirls`` instance for given ``squirls.genome-assembly`` and ``squirls.data-version``::

  GenomicAssemblyVersion genomicAssemblyVersion = GenomicAssemblyVersion.GRCH38;
  String dataVersion = "1902";

  Squirls squirls = squirlsFactory.getSquirls(SquirlsResourceVersion.of(dataVersion, genomicAssemblyVersion));

``Squirls`` provides high-level API for access to the reference genome, to calculate the splice features, and the Squirls
score for given variant.

This is a minimal example for annotating the variant *NM_000251.2:c.1915C>T* (*chr2:47702319C>T*), predicted to
create a novel cryptic donor site in *MSH2*::

  VariantSplicingEvaluator variantEvaluator = squirls.variantSplicingEvaluator();
  GenomicAssembly assembly = squirls.squirlsDataService().genomicAssembly();
  VcfConverter vcfConverter = new VcfConverter(assembly, VariantTrimmer.rightShiftingTrimmer(VariantTrimmer.retainingCommonBase()));

  // chr2	47702319	MSH2_cryptic_donor	C	T	1000	.	AC=2;AF=1	GT	1/1
  Variant variant = vcfConverter.convert(assembly.contigByName("chr2"), "MSH2_cryptic_donor", 47_702_319, "C", "T");
  SquirlsResult squirlsResult = variantEvaluator.evaluate(variant);

  assertThat(squirlsResult.isPathogenic(), is(true));
  assertThat(squirlsResult.maxPathogenicity(), is(closeTo(0.698, 1E-5)));


Use ``SquirlsResult`` for the downstream variant analysis.

Spring Boot application
~~~~~~~~~~~~~~~~~~~~~~~

Squirls includes a ``squirls-spring-boot-starter`` module for including Squirls into an application that uses Spring boot framework.
Using the starter requires even less lines of code than using ``squirls-bootstrap``.

To use Squirls in a Spring boot app, add the following dependency into your ``pom.xml``::

  <dependency>
    <groupId>org.monarchinitiative.squirls</groupId>
    <artifactId>squirls-spring-boot-starter</artifactId>
    <version>${project.version}</version>
  </dependency>

After adding the dependency, Spring configures Squirls beans, as long as the following environment properties are set
to the appropriate values:

- ``squirls.data-directory``
- ``squirls.genome-assembly``
- ``squirls.data-version``

Squirls beans include several high-level objects:

* ``SquirlsDataService`` to get transcripts that overlap with given coordinates, to fetch reference genome sequence, etc.
* ``SplicingAnnotator`` to calculate splice features for variant
* ``SquirlsClassifier`` to calculate Squirls score given splice features, and
* ``VariantSplicingEvaluator`` to perform all described above within a single method call.

See the ``squirls-cli`` module for a real-world example how to use Squirls as a library.

.. _Svart: https://github.com/exomiser/svart
