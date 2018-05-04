# Java Collection Overhead

Demonstrate overheads for various Java Collection implementations.  This
measures storing `int` data, excluding the cost of boxed `Integer`s.
This compares built-in Java, [fastutil](http://fastutil.di.unimi.it/),
[Guava](https://github.com/google/guava), and
[Trove](https://bitbucket.org/trove4j/trove/) collections.
Tested with JDK 8 on x86-64 Linux with
[compressed Oops](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/performance-enhancements-7.html#compressedOop)
(default).

## Results

Measure per-entry overhead by creating a single large collection.  Measure
per-instance overhead by creating many small collections with a single element.
*This over-reports per-instance overhead by 4 bytes due to
`Collection<Collection>`*

| Collection             | Per-entry<br />Overhead (bytes) | Per-instance<br />Overhead (bytes) |
| ---------------------- | ---:| ---:|
| **List**               |     |     |
| ArrayList              |   4 |  52 |
| ImmutableList          |   4 |  20 |
| LinkedList             |  24 |  60 |
| TIntArrayList          |   4 |  52 |
| TIntLinkedList         |  24 |  60 |
|                        |     |     |
| **Map**                |     |     |
| Cache                  |  56 | 916 |
| ConcurrentHashMap      |  40 | 180 |
| ConcurrentSkipListMap  |  36 | 145 |
| EnumMap                | n/a | 124 |
| HashMap                |  40 | 108 |
| ImmutableMap           |  32 |  44 |
| ImmutableRangeMap      |  64 | 116 |
| ImmutableSortedMap     |   8 | 108 |
| Int2IntAVLTreeMap      |  32 | 164 |
| Int2IntLinkedOpenHashMap| 32 | 188 |
| Int2IntOpenHashMap     |  16 | 132 |
| Int2IntRBTreeMap       |  32 | 452 |
| LinkedHashMap          |  48 | 124 |
| MapMaker               |  40 | 244 |
| TIntIntHashMap         |  18 | 156 |
| TreeMap                |  40 |  92 |
| TreeRangeMap           | 120 | 188 |
|                        |     |     |
| **Multiset**           |     |     |
| ConcurrentHashMultiset |  56 | 220 |
| HashMultiset           |  56 | 156 |
| ImmutableMultiset      |  32 | 116 |
| LinkedHashMultiset     |  64 | 172 |
| TreeMultiset           |  56 | 212 |
|                        |     |     |
| **Queue**              |     |     |
| ArrayDeque             |   8 | 108 |
| ConcurrentLinkedDeque  |  24 |  76 |
| PriorityQueue          |   4 |  60 |
|                        |     |     |
| **Set**                |     |     |
| ConcurrentSkipListSet  |  36 | 161 |
| EnumSet                | n/a |  36 |
| HashSet                |  40 | 124 |
| ImmutableRangeSet      |  60 | 100 |
| ImmutableSet           |  12 |  28 |
| ImmutableSortedSet     |   4 |  52 |
| IntAVLTreeSet          |  32 | 140 |
| IntLinkedOpenHashSet   |  24 | 132 |
| IntOpenHashSet         |   8 |  84 |
| IntRBTreeSet           |  32 | 436 |
| LinkedHashSet          |  48 | 140 |
| TIntHashSet            |   8 | 116 |
| TreeRangeSet           |  96 | 180 |
| TreeSet                |  40 | 108 |

Collected via:

```bash
target/java-collection-overhead --list | grep -v Enum | while read c; do target/java-collection-overhead $c $((8 * 1024 * 1024)) 1 || break; done
target/java-collection-overhead --list | while read c; do target/java-collection-overhead $c 1 $((1024 * 1024)) || break; done
```

## References

* https://github.com/DimitrisAndreou/memory-measurer/blob/master/ElementCostInDataStructures.txt
* https://www.ibm.com/developerworks/java/library/j-codetoheap/

## License

Copyright (C) 2018 Andrew Gaul

Licensed under the Apache License, Version 2.0
