.. _rstmodel:


===============
Squirls anatomy
===============

This document outlines the anatomy of the Squirls model, specifically, how a Squirls score is calculated for a variant.

As outlined in the manuscript, Squirls consists of two *site-specific estimators* (one for the donor and the other for
the acceptor site) followed by a *logistic regression*. Both site-specific estimators calculate *site-specific deleteriousness estimate*
(SSDE) for a variant, which are subsequently transformed by the logistic regression to produce the final Squirls score.
The variant is always evaluated wrt. specific transcript.


Splice features
^^^^^^^^^^^^^^^

The first step of the prediction process is the construction of a set of splicing features. The features are constructed
for each variant-transcript combination and the feature values are then passed to site-specific estimators. Please see
the features and


Donor site-specific estimator
#############################

The features used to calculate SSDEs by the donor site-specific estimator:

Donor offset
   Number of 1 bp-long steps required to pass through the exon/intron border of the closest *donor* site. The number is
   negative if the variant is located upstream from the border.
:math:`R_i` *wt* donor
   :term:`Information content` (:math:`R_i`) of the closest canonical donor site.
max :math:`R_i` cryptic donor window
   Maximum :math:`R_i` of sliding window of all 9 bp sequences that contain the *alt* allele.
:math:`\Delta R_i` canonical donor
   Difference between :math:`R_i` of *ref* and *alt* alleles of the closest donor site (0 if the variant does not affect the
   site).
:math:`\Delta R_i` cryptic donor
   Difference between max :math:`R_i` of sliding window of all 9 bp sequences that contain the *alt* allele and :math:`R_i`
   of *alt* allele of the closest donor site.
:math:`\Delta R_i` *wt* closest donor
   Difference between :math:`R_i` of the closest donor and the downstream (3’) donor site (0 if this is the donor site of
   the last intron).
phyloP
   Mean phyloP score of the *ref* allele region.


Acceptor site-specific estimator
################################

The features used to calculate SSDEs by the acceptor site-specific estimator:

Acceptor offset
   Number of 1 bp-long steps required to pass through the exon/intron border of the closest *acceptor* site. The number
   is negative if the variant is located upstream from the border.
:math:`\Delta R_i` canonical acceptor
   Difference between :term:`information content` (:math:`R_i`) of *ref* and *alt* alleles of the closest acceptor site
   (``0`` if the variant does not affect the acceptor site).
:math:`\Delta R_i` cryptic acceptor
   Difference between max :math:`R_i` of sliding window applied to *alt* allele neighboring sequence and :math:`R_i` of
   *alt* allele of the closest acceptor site.
Exon length
   Number of nucleotides spanned by the exon where the variant is located in (``-1`` for non-coding variants that do not
   affect the canonical donor/acceptor regions).
Creates ``AG`` in :term:`AGEZ`
   ``1`` if the variant creates a novel ``AG`` di-nucleotide in :term:`AGEZ`, ``0`` otherwise.
Creates ``YAG`` in :term:`AGEZ`
   ``1`` if the variant creates a novel ``YAG`` tri-nucleotide in :term:`AGEZ` where ``Y`` stands for a pyrimidine
   derivative (cytosine or thymine), ``0`` otherwise.
ESRSeq
   Estimate of impact of random hexamer sequences on splicing efficiency when inserted into five distinct positions of
   two different minigene exons obtained by in vitro screening (`Ke et al., 2011`_).
SMS
   Estimated splicing efficiency for 7-mer sequences obtained by saturating a model exon with single and double base
   substitutions (saturation mutagenesis derived splicing score, `Ke et al., 2018`_).
phyloP
   Mean phyloP score of the *ref* allele region.


Site-specific estimators
^^^^^^^^^^^^^^^^^^^^^^^^

Squirls model consists of two *site-specific estimators*, one for trained to recognize variants that change splicing of
a donor site and the other for the acceptor site. The models calculate *site-specific deleteriousness estimates* (SSDE) for
each variant.

Each estimator consists of an imputer that replaces a missing values with the median value. The imputer is followed by
a random forest classifier. The random forest consists of :math:`n` decision trees that use the splice features to make a decision
regarding deleteriousness of the variant. For variant :math:`v`, a tree :math:`t` outputs probability of variant being
deleterious :math:`p_{del} = t(v)`
that is in range :math:`[0,1]`. Then, using the entire tree set of the forest, SSDE is calculated as
:math:`\frac{1}{n}\sum_{i=0}^{n} t_n(v)`.


Logistic regression
^^^^^^^^^^^^^^^^^^^

Since SSDEs do not span the entire range :math:`[0, 1]` (standard for another tools), we decided to use logistic
regression (LR) as the final step of the prediction process. LR integrates both SSDEs into the final Squirls score.

**TODO** - write more if necessary.

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
