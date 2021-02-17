.. _rstmodel:


===============
Squirls anatomy
===============

This document outlines the anatomy of the Squirls model, specifically, how a Squirls score is calculated for a variant.

As outlined in the `Squirls manuscript`_, Squirls consists of two *random forest estimators*
(one for the donor and the other for the acceptor site) followed by a *logistic regression*.
Both random forests calculate predictions for a single variant, the predictions are subsequently transformed
by the logistic regression into the final Squirls score.
For a single variant, Squirls calculates scores for all overlapping transcripts.

.. _splice-features-ref:

Splice features
^^^^^^^^^^^^^^^

The first step of the prediction process is the calculation of a small set of interpretable numeric features
for machine learning. The features are then passed to random forest estimators. The random forests use different feature
subsets to perform the prediction.


Donor site-specific estimator
#############################

This section lists the features used by the donor random forest estimator:

:math:`R_i` *wt* donor
   :term:`Information content` (:math:`R_i`) of the closest canonical donor site.
:math:`\Delta R_i` canonical donor
   Difference between :math:`R_i` of *ref* and *alt* alleles of the closest donor site
   (0 bits if the variant does not affect the site).
:math:`\Delta R_i` *wt* closest donor
   Difference between :math:`R_i` of the closest donor and the downstream (3’) donor site
   (0 bits if this is the donor site of the last intron).
Donor offset
   Number of 1 bp-long steps required to pass through the exon/intron border of the closest *donor* site. The number is
   negative if the variant is located upstream from the border.
max :math:`R_i` cryptic donor window
   Maximum :math:`R_i` of sliding window of all 9 bp sequences that contain the *alt* allele.
:math:`\Delta R_i` cryptic donor
   Difference between max :math:`R_i` of sliding window of all 9 bp sequences that contain the *alt* allele and :math:`R_i`
   of *alt* allele of the closest donor site.
phyloP
   Mean phyloP score of the *ref* allele region.


Acceptor site-specific estimator
################################

These are the features used by the acceptor random forest estimator:

:math:`\Delta R_i` canonical acceptor
   Difference between :term:`information content` (:math:`R_i`) of *ref* and *alt* alleles of the closest acceptor site
   (``0`` if the variant does not affect the acceptor site).
:math:`\Delta R_i` cryptic acceptor
   Difference between max :math:`R_i` of sliding window applied to *alt* allele neighboring sequence and :math:`R_i` of
   *alt* allele of the closest acceptor site.
Creates ``AG`` in :term:`AGEZ`
   ``1`` if the variant creates a novel ``AG`` di-nucleotide in :term:`AGEZ`, ``0`` otherwise.
Creates ``YAG`` in :term:`AGEZ`
   ``1`` if the variant creates a novel ``YAG`` tri-nucleotide in :term:`AGEZ` where ``Y`` stands for a pyrimidine
   derivative (cytosine or thymine), ``0`` otherwise (see `Wimmer et al., 2020`_).
Acceptor offset
   Number of 1 bp-long steps required to pass through the exon/intron border of the closest *acceptor* site. The number
   is negative if the variant is located upstream from the border.
Exon length
   Number of nucleotides spanned by the exon where the variant is located in (``-1`` for non-coding variants that do not
   affect the canonical donor/acceptor regions).
ESRSeq
   Estimate of impact of random hexamer sequences on splicing efficiency when inserted into five distinct positions of
   two different minigene exons obtained by in vitro screening (`Ke et al., 2011`_).
SMS
   Estimated splicing efficiency for 7-mer sequences obtained by saturating a model exon with single and double base
   substitutions (saturation mutagenesis derived splicing score, `Ke et al., 2018`_).
phyloP
   Mean phyloP score of the *ref* allele region.

.. note::
  The values of all features based on information theory are in *bits* of information

Random forest estimators
^^^^^^^^^^^^^^^^^^^^^^^^

Squirls algorithm consists of two *random forest estimators* trained to recognize variants that change splicing of
a donor or acceptor site. Given a set of splice features, the estimator calculates deleteriousness for the corresponding
variant.

If a feature cannot be calculated for a variant, the missing feature value is imputed by a median feature value
that was observed during training of the model.

The random forest consists of :math:`n` decision trees that use the splice features to make a decision
regarding deleteriousness of the variant in question.


Logistic regression
^^^^^^^^^^^^^^^^^^^

Squirls uses logistic regression as the final step to integrate outputs of the donor and acceptor random forests into
the final Squirls score.

Glossary
^^^^^^^^

.. glossary::
   :sorted:

   **Information content**
      Individual information content of a nucleotide sequence :math:`R_i(j)` that is related to thermodynamic entropy
      and the free energy of binding. :math:`R_i` can also be used to compare sites with one another.

   **AGEZ**
      AG‐exclusion zone, the sequence between the branch point and the proper 3'ss ``AG`` that is devoid of ``AG``\ s, as
      defined by `Gooding et al., 2006`_

.. _Ke et al., 2011: https://pubmed.ncbi.nlm.nih.gov/21659425
.. _Ke et al., 2018: https://pubmed.ncbi.nlm.nih.gov/29242188
.. _Gooding et al., 2006: https://pubmed.ncbi.nlm.nih.gov/16507133
.. _Wimmer et al., 2020: https://pubmed.ncbi.nlm.nih.gov/32126153
.. TODO - update the manuscript link
.. _Squirls manuscript:  https://www.biorxiv.org/content/10.1101/2021.01.28.428499v1
