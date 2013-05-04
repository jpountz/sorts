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
import com.carrotsearch.randomizedtesting.annotations.Repeat;

@RunWith(RandomizedRunner.class)
public class TimSorterTest extends AbstractSortTest {

  public TimSorterTest() {
    super(true);
  }

  @Override
  public Sorter newSorter(Entry[] arr) {
    return new ArrayTimSorter<Entry>(arr, randomInt(arr.length));
  }

  @Test
  @Repeat(iterations=10)
  public void testLowerUpper() {
    final Integer[] arr = new Integer[randomIntBetween(10, 100)];
    final int max = randomInt(20);
    for (int i = 0; i < arr.length; ++i) {
      arr[i] = randomInt(max);
    }
    Arrays.sort(arr);
    final TimSorter sorter = new ArrayTimSorter<Integer>(arr, arr.length);
    final int savedStart = randomInt(arr.length / 2);
    final int savedLength = randomIntBetween(1, arr.length - savedStart);
    sorter.saveAll(savedStart, savedLength);
    final int savedOff = randomInt(savedLength - 1);
    final int off = savedStart + savedOff;
    final int from = randomInt(arr.length / 2);
    final int to = randomIntBetween(arr.length / 2, arr.length);

    assertEquals(sorter.lower(from, to, off), sorter.lowerSaved(from, to, savedOff));
    assertEquals(sorter.lower(from, to, off), sorter.lowerSaved3(from, to, savedOff));
    assertEquals(sorter.upper(from, to, off), sorter.upperSaved(from, to, savedOff));
    assertEquals(sorter.upper(from, to, off), sorter.upperSaved3(from, to, savedOff));
  }

}
