package net.jpountz.sorts;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Benchmark {

  public static void main(String[] args) {
    final Integer[] array = new Integer[5000000];
    final List<Integer> list = Arrays.asList(array);
    final Random random = new Random();
    for (int i = 0; i < array.length; ++i) {
      array[i] = random.nextInt(100);
    }
    final Sorter quickSorter = new ArrayQuickSorter<Integer>(array);
    final Sorter heapSorter = new ArrayHeapSorter<Integer>(array);
    final Sorter mergeSorter = new ArrayMergeSorter<Integer>(array);
    final Sorter inPlaceMergeSorter = new ArrayInPlaceMergeSorter<Integer>(array);
    final Sorter lowMemMergeSorter = new ArrayLowMemoryMergeSorter<Integer>(array, array.length / 100);
    final int from = 0, to = array.length;
    long start = System.nanoTime();
    while (System.nanoTime() - start < 10L * 1000 * 1000 * 1000) {
      Collections.shuffle(list);
      Arrays.sort(array);
      Collections.shuffle(list);
      quickSorter.sort(from, to);
      Collections.shuffle(list);
      heapSorter.sort(from, to);
      Collections.shuffle(list);
      mergeSorter.sort(from, to);
      Collections.shuffle(list);
      inPlaceMergeSorter.sort(from, to);
      Collections.shuffle(list);
      lowMemMergeSorter.sort(from, to);
    }
    for (int i = 0; i < 10; ++i) {
      Collections.shuffle(list);
      start = System.nanoTime();
      Arrays.sort(array);
      System.out.println("Arrays.sort: " + (System.nanoTime() - start) / 1000 / 1000 + " ms");
      Collections.shuffle(list);
      start = System.nanoTime();
      quickSorter.sort(from, to);
      System.out.println("Quicksort: " + (System.nanoTime() - start) / 1000 / 1000 + " ms");
      Collections.shuffle(list);
      start = System.nanoTime();
      heapSorter.sort(from, to);
      System.out.println("HeapSort: " + (System.nanoTime() - start) / 1000 / 1000 + " ms");
      Collections.shuffle(list);
      start = System.nanoTime();
      mergeSorter.sort(from, to);
      System.out.println("Mergesort: " + (System.nanoTime() - start) / 1000 / 1000 + " ms");
      Collections.shuffle(list);
      start = System.nanoTime();
      inPlaceMergeSorter.sort(from, to);
      System.out.println("In-place Mergesort: " + (System.nanoTime() - start) / 1000 / 1000 + " ms");
      Collections.shuffle(list);
      start = System.nanoTime();
      lowMemMergeSorter.sort(from, to);
      System.out.println("Low-mem Mergesort: " + (System.nanoTime() - start) / 1000 / 1000 + " ms");
    }
  }

}
