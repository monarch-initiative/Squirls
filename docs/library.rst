.. _rstlibrary:

========================
Use Squirls as a library
========================

Squirls has been designed to be used as a library within a larger frameworks for prioritization of genome variants. This
document explains how to use Squirls *API* classes to enable including splicing deleteriousness predictions into a Java
software.

The following sections describe how to use Squirls as a module in other Java tool.

Install Squirls modules into your local Maven repository
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Please run the following command to install Squirls into the local Maven repository::

  git checkout https://github.com/TheJacksonLaboratory/Squirls
  cd Squirls
  ./mvnw install

.. note::
  The installation requires JDK11+ to be present on the platform

Spring Boot application
~~~~~~~~~~~~~~~~~~~~~~~

After the successful installation, add the following dependency into your ``pom.xml`` to use Squirls in a Spring boot app::

  <dependency>
    <groupId>org.monarchinitiative.squirls</groupId>
    <artifactId>squirls-spring-boot-starter</artifactId>
    <version>1.0.0</version>
  </dependency>

After adding the dependency, Spring configures ``VariantSplicingEvaluator`` bean, provided that the environment properties

- ``squirls.data-directory``
- ``squirls.genome-assembly``
- ``squirls.data-version``

are set to proper values.

The ``VariantSplicingEvaluator`` provides the following methods to calculate ``SquirlsResult`` for a ``Variant`` specified by
`Svart`_ library::

  SquirlsResult evaluate(Variant variant, Set<String> txIds);
  SquirlsResult evaluate(Variant variant);

Please see the corresponding *Javadocs* to learn more about ``VariantSplicingEvaluator``, ``SquirlsResult``, etc.

.. _Svart: https://github.com/exomiser/svart
