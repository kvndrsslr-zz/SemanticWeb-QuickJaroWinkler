# Purpose

This package was written for an academic practical course during my bachelor studies in computer science.
In the cutting-edge reasearch field of semantic web one has to handle very large (>10^6 entries) knowledge bases.
These knowledge bases may be linked to discover new knowledge.
It is absolutely essential to real world applications to filter the data before attempting any fuzzy string matching algorithm, because matching two large knowledge bases may result in 10^12 and more string comparisons, which is very expensive.
This package implements range and individual filters with good scaling properties for the Jaro-Winkler algorithm which runs in O(n*m), n and m being the input strings lenths.