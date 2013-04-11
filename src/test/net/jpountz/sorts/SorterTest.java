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
import org.junit.runner.RunWith;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

@RunWith(RandomizedRunner.class)
public class SorterTest extends RandomizedTest {

  @Test
  @Repeat(iterations=10)
  public void testLowerUpper() {
    final Integer[] arr = new Integer[randomIntBetween(10, 100)];
    for (int i = 0; i < arr.length; ++i) {
      arr[i] = randomInt(20);
    }
    Arrays.sort(arr);
    final Sorter sorter = new ArrayHeapSorter<Integer>(arr);
    final int off = randomInt(arr.length - 1);
    final int from = randomInt(arr.length / 2);
    final int to = randomIntBetween(arr.length / 2, arr.length);

    int dest = sorter.lower(from, to, off);
    if (dest > from) {
      assertTrue(arr[off] > arr[dest - 1]);
    }
    if (dest < to) {
      assertTrue(arr[off] <= arr[dest]);
    }

    int dest2 = sorter.lower2(from, to, off);
    assertEquals(dest, dest2);

    dest = sorter.upper(from, to, off);
    if (dest > from) {
      assertTrue(arr[off] >= arr[dest - 1]);
    }
    if (dest < to) {
      assertTrue(arr[off] < arr[dest]);
    }

    dest2 = sorter.upper2(from, to, off);
    assertEquals(dest, dest2);
  }

}
