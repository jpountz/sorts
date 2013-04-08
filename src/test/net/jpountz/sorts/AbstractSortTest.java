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

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

public abstract class AbstractSortTest extends RandomizedTest {

  private final boolean stable;

  public AbstractSortTest(boolean stable) {
    this.stable = stable;
  }

  public abstract Sorter newSorter(Entry[] arr);

  public void assertSorted(Entry[] original, Entry[] sorted) {
    assertEquals(original.length, sorted.length);
    Entry[] actuallySorted = Arrays.copyOf(original, original.length);
    Arrays.sort(actuallySorted);
    for (int i = 0; i < original.length; ++i) {
      assertEquals(actuallySorted[i].value, sorted[i].value);
      if (stable) {
        assertEquals(actuallySorted[i].ord, sorted[i].ord);
      }
    }
  }

  public void test(Entry[] arr) {
    final Entry[] sorted = Arrays.copyOf(arr, arr.length);
    newSorter(sorted).sort(0, sorted.length);
    assertSorted(arr, sorted);
  }

  enum Strategy {
    RANDOM {
      @Override
      public void set(Entry[] arr, int i) {
        arr[i] = new Entry(randomInt(), i);
      }
    },
    RANDOM_LOW_CARDINALITY {
      @Override
      public void set(Entry[] arr, int i) {
        arr[i] = new Entry(randomInt(5), i);
      }
    },
    ASCENDING {
      @Override
      public void set(Entry[] arr, int i) {
        arr[i] = i == 0
            ? new Entry(randomInt(5), 0)
            : new Entry(arr[i - 1].value + randomInt(5), i);
      }
    },
    DESCENDING {
      @Override
      public void set(Entry[] arr, int i) {
        arr[i] = i == 0
            ? new Entry(randomInt(5), 0)
            : new Entry(arr[i - 1].value - randomInt(5), i);
      }
    },
    STRICTLY_DESCENDING {
      @Override
      public void set(Entry[] arr, int i) {
        arr[i] = i == 0
            ? new Entry(randomInt(5), 0)
            : new Entry(arr[i - 1].value - randomIntBetween(1, 5), i);
      }
    },
    ASCENDING_SEQUENCES {
      @Override
      public void set(Entry[] arr, int i) {
        arr[i] = i == 0
            ? new Entry(randomInt(5), 0)
            : new Entry(rarely() ? randomInt(5) : arr[i - 1].value + randomInt(5), i);
      }
    };
    public abstract void set(Entry[] arr, int i);
  }

  public void test(Strategy strategy) {
    final int length = randomInt(20000);
    final Entry[] arr = new Entry[length];
    for (int i = 0; i < arr.length; ++i) {
      strategy.set(arr, i);
    }
    test(arr);
  }

  @Test
  public void testRandom() {
    test(Strategy.RANDOM);
  }

  @Test
  public void testRandomLowCardinality() {
    test(Strategy.RANDOM_LOW_CARDINALITY);
  }

  @Test
  public void testAscending() {
    test(Strategy.ASCENDING);
  }

  @Test
  public void testAscendingSequences() {
    test(Strategy.ASCENDING_SEQUENCES);
  }

  @Test
  public void testDescending() {
    test(Strategy.DESCENDING);
  }
  
  @Test
  public void testStrictlyDescending() {
    test(Strategy.STRICTLY_DESCENDING);
  }
}
