# Large Integer Sets in Java

The storing of a large count of integer values is a frequently problem for software developers.
Depending the requirement speed or memory usage is the problem. 
Java contains some implementations for general usage but not optimized for a large count of values.

This project contains a small benchmark test and some implementations of sets for integers.

## Result

The follow results are made with one million integer values on 32 bit Java 8 VM.

Type | Memory in MB | add() in ms | remove() in ms | contains() in ms | iterate() in ms
---- | ------------ | ----------- | -------------- | ---------------- | ---------------
sets.IntCompactSet | 6.01 | 176 | 109 | 47 | 114
sets.CompactSet | 21.27 | 377 | 64 | 69 | 44
java.util.HashSet | 46.15 | 819 | 66 | 37 | 37
java.util.LinkedHashSet | 53.77 | 907 | 75 | 39 | 28