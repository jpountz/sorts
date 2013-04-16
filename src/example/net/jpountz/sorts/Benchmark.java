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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Benchmark {

  static final Random R = new Random();
  static Integer[] CACHE = new Integer[1 << 24];
  static {
    for (int i = 0; i < CACHE.length; ++i) {
      CACHE[i] = i;
    }
  }

  private static enum Order {
    RANDOM {
      @Override
      void prepare(Integer[] arr) {
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = CACHE[R.nextInt(CACHE.length)];
        }
      }
    },
    RANDOM_LOW_CARDINALITY {
      @Override
      void prepare(Integer[] arr) {
        for (int i = 0; i < arr.length; ++i) {
          arr[i] = CACHE[R.nextInt(100)];
        }
      }
    },
    ASCENDING {
      @Override
      void prepare(Integer[] arr) {
        RANDOM.prepare(arr);
        Arrays.sort(arr);
      }
    },
    ASCENDING_SEQUENCES {
      @Override
      void prepare(Integer[] arr) {
        arr[0] = CACHE[R.nextInt(100)];
        for (int i = 1; i < arr.length; ++i) {
          if (R.nextInt(200) == 0) {
            arr[i] = CACHE[R.nextInt(100)];
          } else {
            final int slot = arr[i-1] + R.nextInt(100);
            arr[i] = CACHE[slot & (CACHE.length - 1)];
          }
        }
      }
    },
    MOSTLY_ASCENDING {
      @Override
      void prepare(Integer[] arr) {
        arr[0] = CACHE[R.nextInt(CACHE.length)];
        for (int i = 1; i < arr.length; ++i) {
          final int slot = arr[i-1] - 4 + R.nextInt(10);
          arr[i] = CACHE[slot & (CACHE.length - 1)];
        }
      }
    },
    DESCENDING {
      @Override
      void prepare(Integer[] arr) {
        ASCENDING.prepare(arr);
        Collections.reverse(Arrays.asList(arr));
      }
    },
    STRICTLY_DESCENDING {
      @Override
      void prepare(Integer[] arr) {
        arr[0] = CACHE[CACHE.length - 1 - R.nextInt(10)];
        for (int i = 1; i < arr.length; ++i) {
          arr[i] = arr[i - 1] - 1 - R.nextInt(5);
        }
      }
    };
    abstract void prepare(Integer[] arr);
  }

  public static void main(String[] args) {
    final Integer[] array = new Integer[2000000];
    final Random random = new Random();
    for (int i = 0; i < array.length; ++i) {
      array[i] = random.nextInt(100);
    }

    final Map<String, Sorter> sorters = new LinkedHashMap<String, Sorter>();
    sorters.put("Arrays.sort", new Sorter() {
      @Override
      protected int compare(int i, int j) { throw new UnsupportedOperationException(); }
      @Override
      protected void swap(int i, int j) { throw new UnsupportedOperationException(); }
      @Override
      public void sort(int from, int to) {
        if (from == 0 && to == array.length) {
          Arrays.sort(array);
        } else {
          throw new Error();
        }
      }
    });
    sorters.put("IntroSorter", new ArrayIntroSorter<Integer>(array));
    sorters.put("HeapSorter", new ArrayHeapSorter<Integer>(array));
    sorters.put("TernaryHeapSorter", new ArrayTernaryHeapSorter<Integer>(array));
    sorters.put("MergeSorter", new ArrayMergeSorter<Integer>(array));
    sorters.put("InPlaceMergeSorter", new ArrayInPlaceMergeSorter<Integer>(array));
    sorters.put("TimSorter", new ArrayTimSorter<Integer>(array));
    sorters.put("InPlaceTimSorter", new ArrayInPlaceTimSorter<Integer>(array));
    sorters.put("LowMemoryTimSorter", new ArrayLowMemoryTimSorter<Integer>(array, array.length / 20));

    long start = System.nanoTime();
    // JVM warming
    while (System.nanoTime() - start < 10L * 1000 * 1000 * 1000) {
      for (Sorter sorter : sorters.values()) {
        Order.RANDOM.prepare(array);
        sorter.sort(0, array.length);
      }
    }
    for (Order order : Order.values()) {
      System.out.print('\t');
      System.out.print(order);
    }
    System.out.println();
    for (int i = 0; i < 10; ++i) {
      for (Map.Entry<String, Sorter> entry : sorters.entrySet()) {
        final Sorter sorter = entry.getValue();
        System.out.print(entry.getKey());
        for (Order order : Order.values()) {
          order.prepare(array);
          start = System.nanoTime();
          sorter.sort(0, array.length);
          final long time = (System.nanoTime() - start) / 1000 / 1000;
          System.out.print('\t');
          System.out.print(time); // ms
        }
        System.out.println();
      }
    }
  }

}
