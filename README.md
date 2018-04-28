# Large Integer Sets in Java

The storing of a large count of integer values is a frequently problem for software developers.
Depending the requirement, speed or memory usage is the problem. 
Java contains some implementations for general usage but not optimized for a large count of values.

This project contains a small benchmark test and some implementations of sets for integers.

## Result

The follow results are made with one million integer values on 32 bit Java 8 VM.

Type | Memory in MB | add() in ms | remove() in ms | contains() in ms | iterate() in ms
---- | ------------ | ----------- | -------------- | ---------------- | ---------------
sets.PagedIntSet | 3.57 | 197 | 67 | 58 | 48
sets.IntCompactSet | 6.01 | 178 | 97 | 41 | 109
sets.CompactSet | 21.27 | 358 | 58 | 77 | 44
java.util.HashSet | 46.15 | 768 | 67 | 38 | 37
java.util.LinkedHashSet | 53.78 | 919 | 78 | 43 | 29

The memory consume relative exact. The timing is quite inaccurate and should not be overstated.

## Warranty
There is no guarantee that the integer sets here are error-free and 100% API-compatible.

## Contribution
If you finds bugs in the sample code or you you have some other good implementations of a Set for Integers then your contribution is welcome.