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

## Per-entry overhead

Create a single large collection then measure the per-entry overhead.

```bash
target/java-collection-overhead --list | grep -v Enum | while read c; do target/java-collection-overhead $c $((8 * 1024 * 1024)) 1 || break; done
```

| Collection             |  Overhead |
| ---------------------- | ---------:|
| ArrayDeque             |         8 |
| ArrayList              |         4 |
| Cache                  |        56 |
| ConcurrentHashMap      |        40 |
| ConcurrentHashMultiset |        56 |
| ConcurrentLinkedDeque  |        24 |
| ConcurrentSkipListMap  |        36 |
| ConcurrentSkipListSet  |        36 |
| HashMap                |        40 |
| HashMultiset           |        56 |
| HashSet                |        40 |
| ImmutableMap           |        32 |
| ImmutableMultiset      |        32 |
| ImmutableRangeMap      |        64 |
| ImmutableSet           |        12 |
| ImmutableSortedMap     |         8 |
| ImmutableSortedSet     |         4 |
| LinkedHashMap          |        48 |
| LinkedHashMultiset     |        64 |
| LinkedHashSet          |        48 |
| LinkedList             |        24 |
| MapMaker               |        40 |
| PriorityQueue          |         4 |
| TLongArrayList         |         8 |
| TLongHashSet           |        18 |
| TLongLinkedList        |        32 |
| TLongLongHashMap       |        34 |
| TreeMap                |        40 |
| TreeMultiset           |        56 |
| TreeRangeMap           |       120 |
| TreeSet                |        40 |

## Per-instance overhead

Create many small collections then measure the overhead for a collection with a
single element.

TODO: this over-reports by 4 bytes due to `Collection<Collection>`

```bash
target/java-collection-overhead --list | while read c; do target/java-collection-overhead $c 1 $((1024 * 1024)) || break; done
```

| Collection             |  Overhead |
| ---------------------- | ---------:|
| ArrayDeque             |       108 |
| ArrayList              |        52 |
| Cache                  |       916 |
| ConcurrentHashMap      |       180 |
| ConcurrentHashMultiset |       220 |
| ConcurrentLinkedDeque  |        76 |
| ConcurrentSkipListMap  |       145 |
| ConcurrentSkipListSet  |       161 |
| EnumMap                |       124 |
| EnumSet                |        36 |
| HashMap                |       108 |
| HashMultiset           |       156 |
| HashSet                |       124 |
| ImmutableList          |        20 |
| ImmutableMap           |        44 |
| ImmutableMultiset      |       116 |
| ImmutableRangeMap      |       116 |
| ImmutableSet           |        28 |
| ImmutableSortedMap     |       108 |
| ImmutableSortedSet     |        52 |
| LinkedHashMap          |       124 |
| LinkedHashMultiset     |       172 |
| LinkedHashSet          |       140 |
| LinkedList             |        60 |
| MapMaker               |       244 |
| PriorityQueue          |        60 |
| TLongArrayList         |        60 |
| TLongHashSet           |       132 |
| TLongLinkedList        |        68 |
| TLongLongHashMap       |       180 |
| TreeMap                |        92 |
| TreeMultiset           |       212 |
| TreeRangeMap           |       188 |
| TreeSet                |       108 |

## References

* https://github.com/DimitrisAndreou/memory-measurer/blob/master/ElementCostInDataStructures.txt
* https://www.ibm.com/developerworks/java/library/j-codetoheap/

## License

Copyright (C) 2018 Andrew Gaul

Licensed under the Apache License, Version 2.0
