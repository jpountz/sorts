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

/**
 * A {@link Sorter} implementation based on the insertion sort algorithm.
 * <p>This sort algorithm is stable and is best used on small arrays which are
 * almost sorted.
 */
public abstract class InsertionSorter extends Sorter {

  /** Create a new {@link InsertionSorter} */
  public InsertionSorter() {}

  @Override
  public final void sort(int from, int to) {
    checkRange(from, to);
    insertionSort(from, to);
  }

}
