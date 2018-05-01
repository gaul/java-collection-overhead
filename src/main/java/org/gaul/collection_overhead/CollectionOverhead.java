/*
 * Copyright 2018 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gaul.java_collection_overhead;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Queues;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeMultiset;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import com.google.common.io.CharStreams;

import gnu.trove.TLongCollection;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.linked.TLongLinkedList;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.hash.TLongHashSet;

/** Demonstrate Java and Guava Collection overheads. */
public final class CollectionOverhead {
    private static final Collection<String> COLLECTIONS = Arrays.asList(
            "ArrayDeque",
            "ArrayList",
            "Cache",
            "ConcurrentHashMap",
            "ConcurrentHashMultiset",
            "ConcurrentLinkedDeque",
            "ConcurrentSkipListMap",
            "ConcurrentSkipListSet",
            "EnumMap",
            "EnumSet",
            "HashMap",
            "HashMultiset",
            "HashSet",
            "ImmutableList",
            "ImmutableMap",
            "ImmutableMultiset",
            "ImmutableRangeMap",
            "ImmutableRangeSet",
            "ImmutableSet",
            "ImmutableSortedMap",
            "ImmutableSortedSet",
            "LinkedHashMap",
            "LinkedHashMultiset",
            "LinkedHashSet",
            "LinkedList",
            "MapMaker",
            "PriorityQueue",
            "TLongArrayList",
            "TLongLinkedList",
            "TLongLongHashMap",
            "TLongHashSet",
            "TreeMap",
            "TreeMultiset",
            "TreeRangeMap",
            "TreeRangeSet",
            "TreeSet"
    );

    private CollectionOverhead() {
        throw new AssertionError("not implemented");
    }

    public static void main(final String[] args) throws IOException {
        if (args.length == 1 && args[0].equals("--list")) {
            for (String collection : COLLECTIONS) {
                System.out.println(collection);
            }
            System.exit(0);
        }
        if (args.length != 3) {
            System.out.println("Usage: java CollectionOverhead" +
                    " collection-type collection-size num-collections");
            System.exit(1);
        }
        String collectionType = args[0];
        int collectionSize = Integer.parseInt(args[1], /*radix=*/ 10);
        int numCollections = Integer.parseInt(args[2], /*radix=*/ 10);

        Collection<Object> objects = new ArrayList<>(numCollections);
        for (int i = 0; i < numCollections; ++i) {
            objects.add(populateCollection(args[0], collectionSize));
        }

        forceGarbageCollection();

        String output = runCommand(new String[] {
            "sh", "-c", "jmap -histo:live $PPID" +
                " | awk '$4 !~ /java.lang.Integer/" +
                " && $1 !~ /Total/{x += $3} END{print x}'"});
        long numBytes = Long.parseLong(output.trim());
        System.out.printf("%-24s%8d%8d\n", args[0],
                numBytes / numCollections,
                (long) (numBytes / (double) collectionSize) / numCollections);

        // prevent GC of map until after jmap
        Object object = objects;
    }

    @SuppressWarnings("JdkObsolete")
    private static Object populateCollection(String type, int size) {
        Collection<Integer> collection = null;
        Map<Integer, Integer> map = null;
        EnumMap<TestEnum, TestEnum> enumMap = null;
        EnumSet<TestEnum> enumSet = null;
        ImmutableCollection.Builder<Integer> collectionBuilder = null;
        ImmutableMap.Builder<Integer, Integer> mapBuilder = null;
        RangeMap<Integer, Integer> rangeMap = null;
        RangeSet<Integer> rangeSet = null;
        ImmutableRangeMap.Builder<Integer, Integer> rangeMapBuilder = null;
        ImmutableRangeSet.Builder<Integer> rangeSetBuilder = null;
        TLongCollection troveCollection = null;
        TLongLongHashMap troveHashMap = null;

        if (type.equals("ArrayDeque")) {
            collection = Queues.newArrayDeque();
        } else if (type.equals("ArrayList")) {
            collection = new ArrayList<>(size);
        } else if (type.equals("Cache")) {
            Cache<Integer, Integer> cache = CacheBuilder.newBuilder().build();
            map = cache.asMap();
        } else if (type.equals("ConcurrentHashMap")) {
            map = new ConcurrentHashMap<>(/*16, 0.75f, 16*/);
        } else if (type.equals("ConcurrentHashMultiset")) {
            collection = ConcurrentHashMultiset.create();
        } else if (type.equals("ConcurrentLinkedDeque")) {
            collection = new ConcurrentLinkedDeque<>();
        } else if (type.equals("ConcurrentSkipListMap")) {
            map = new ConcurrentSkipListMap<>();
        } else if (type.equals("ConcurrentSkipListSet")) {
            collection = new ConcurrentSkipListSet<>();
        } else if (type.equals("EnumMap")) {
            enumMap = new EnumMap<>(TestEnum.class);
        } else if (type.equals("EnumSet")) {
            enumSet = EnumSet.noneOf(TestEnum.class);
        } else if (type.equals("HashMap")) {
            map = new HashMap<>(size);
        } else if (type.equals("HashMultiset")) {
            collection = HashMultiset.create(size);
        } else if (type.equals("HashSet")) {
            collection = new HashSet<>(size);
        } else if (type.equals("ImmutableList")) {
            collectionBuilder = ImmutableList.builder();
        } else if (type.equals("ImmutableMap")) {
            mapBuilder = ImmutableMap.builder();
        } else if (type.equals("ImmutableMultiset")) {
            collectionBuilder = ImmutableMultiset.builder();
        } else if (type.equals("ImmutableRangeMap")) {
            rangeMapBuilder = ImmutableRangeMap.builder();
        } else if (type.equals("ImmutableRangeSet")) {
            rangeSetBuilder = ImmutableRangeSet.builder();
        } else if (type.equals("ImmutableSet")) {
            collectionBuilder = ImmutableSet.builder();
        } else if (type.equals("ImmutableSortedMap")) {
            mapBuilder = ImmutableSortedMap.naturalOrder();
        } else if (type.equals("ImmutableSortedSet")) {
            collectionBuilder = ImmutableSortedSet.naturalOrder();
        } else if (type.equals("LinkedHashMap")) {
            map = new LinkedHashMap<>(size);
        } else if (type.equals("LinkedHashMultiset")) {
            collection = LinkedHashMultiset.create(size);
        } else if (type.equals("LinkedHashSet")) {
            collection = new LinkedHashSet<>(size);
        } else if (type.equals("LinkedList")) {
            collection = new LinkedList<>();
        } else if (type.equals("MapMaker")) {
            map = new MapMaker().concurrencyLevel(1).makeMap();
        } else if (type.equals("PriorityQueue")) {
            collection = new PriorityQueue<Integer>(size);
        } else if (type.equals("TLongArrayList")) {
            troveCollection = new TLongArrayList(size);
        } else if (type.equals("TLongHashSet")) {
            troveCollection = new TLongHashSet(size);
        } else if (type.equals("TLongLinkedList")) {
            troveCollection = new TLongLinkedList(size);
        } else if (type.equals("TLongLongHashMap")) {
            troveHashMap = new TLongLongHashMap(size);
        } else if (type.equals("TreeMap")) {
            map = new TreeMap<>();
        } else if (type.equals("TreeMultiset")) {
            collection = TreeMultiset.create();
        } else if (type.equals("TreeRangeMap")) {
            rangeMap = TreeRangeMap.create();
        } else if (type.equals("TreeRangeSet")) {
            rangeSet = TreeRangeSet.create();
        } else if (type.equals("TreeSet")) {
            collection = new TreeSet<>();
        } else {
            throw new IllegalStateException(
                    "could not find collection: " + type);
        }

        if (collection != null) {
            for (int i = 0; i < size; ++i) {
                collection.add(i);
            }
            return collection;
        } else if (map != null) {
            for (int i = 0; i < size; ++i) {
                Integer ii = i;
                map.put(ii, ii);
            }
            return map;
        } else if (enumMap != null) {
            TestEnum[] values = TestEnum.values();
            for (int i = 0; i < size && i < values.length; ++i) {
                enumMap.put(values[i], values[i]);
            }
            return enumMap;
        } else if (enumSet != null) {
            TestEnum[] values = TestEnum.values();
            for (int i = 0; i < size && i < values.length; ++i) {
                enumSet.add(values[i]);
            }
            return enumSet;
        } else if (mapBuilder != null) {
            for (int i = 0; i < size; ++i) {
                Integer ii = i;
                mapBuilder.put(ii, ii);
            }
            return mapBuilder.build();
        } else if (collectionBuilder != null) {
            for (int i = 0; i < size; ++i) {
                collectionBuilder.add(i);
            }
            return collectionBuilder.build();
        } else if (rangeMap != null) {
            for (int i = 0; i < size; ++i) {
                Integer ii = i;
                rangeMap.put(Range.closedOpen(ii, i + 1), ii);
            }
            return rangeMap;
        } else if (rangeSet != null) {
            for (int i = 0; i < size; ++i) {
                // ensure that range is sparse
                rangeSet.add(Range.closedOpen(2 * i, 2 * i + 1));
            }
            return rangeSet;
        } else if (rangeMapBuilder != null) {
            for (int i = 0; i < size; ++i) {
                Integer ii = i;
                rangeMapBuilder.put(Range.closedOpen(ii, i + 1), ii);
            }
            return rangeMapBuilder.build();
        } else if (rangeSetBuilder != null) {
            for (int i = 0; i < size; ++i) {
                // ensure that range is sparse
                rangeSetBuilder.add(Range.closedOpen(2 * i, 2 * i + 1));
            }
            return rangeSetBuilder.build();
        } else if (troveCollection != null) {
            for (int i = 0; i < size; ++i) {
                troveCollection.add(i);
            }
            return troveCollection;
        } else if (troveHashMap != null) {
            for (int i = 0; i < size; ++i) {
                troveHashMap.put(i, i);
            }
            return troveHashMap;
        } else {
            throw new IllegalStateException("Could not find collection");
        }
    }

    private static void forceGarbageCollection() {
        Object obj = new Object();
        WeakReference<Object> ref = new WeakReference<Object>(obj);
        obj = null;
        while (ref.get() != null) {
            System.gc();
        }
    }

    // TODO: exit code
    private static String runCommand(final String[] args) throws IOException {
        Process child = Runtime.getRuntime().exec(args);
        InputStream is = child.getInputStream();
        try {
            return CharStreams.toString(new InputStreamReader(is,
                    StandardCharsets.UTF_8));
        } finally {
            is.close();
        }
    }

    private enum TestEnum {
        A0, A1, A2, A3,
        B0, B1, B2, B3,
        C0, C1, C2, C3,
        D0, D1, D2, D3;
    }
}
