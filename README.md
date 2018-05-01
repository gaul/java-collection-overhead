# Java Collection Overhead

Demonstrate overheads for various Java Collection implementations.  This
measures storing `Long` data, excluding the cost of the key and value objects.
Note that specialized collections for `long` report more overhead than those
that store `Long` since the former stores inline as an 8-byte primitive but the
latter stores a 4-byte reference to an external 16-byte object.  Presently
comparing built-in Java, Guava, and Trove collections.  Tested with JDK 8 on
x86-64 Linux with
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
| TLongArrayList         |   8 |  60 |
| TLongLinkedList        |  32 |  68 |
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
| LinkedHashMap          |  48 | 124 |
| MapMaker               |  40 | 244 |
| TLongLongHashMap       |  34 | 180 |
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
| LinkedHashSet          |  48 | 140 |
| TLongHashSet           |  18 | 132 |
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
