# Purpose

This package was written for an academic practical course during my bachelor studies in computer science.
In the cutting-edge reasearch field of semantic web one has to handle very large (more than 10 million entries) knowledge bases.
These knowledge bases are being linked to each other to discover hidden knowledge or simply interlink them to provide richer content for potential (automated) readers.
It is absolutely essential to real world applications to filter the data pairs before attempting any fuzzy string matching algorithm, because naively matching every pair of two large knowledge bases may result in trillions of string comparisons and these fuzzy matching algorithms come with a considerable amount complexity of their own.
This package implements range and individual filters with good scaling properties for the Jaro-Winkler algorithm.

# Further Information

An online publication with further explanation of the used filters and real data evaluation can be found [here] (https://www.sharelatex.com/project/537233ccc0f23b3d4fd7276c/output/output.pdf).
