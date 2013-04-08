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

/** {@link Sorter} implementation based on merge-sort where the amount of extra
 *  memory that can be used is configurable: small merges will be made using
 *  extra memory while large merges will be performed in-place. Small arrays
 *  are sorted using {@link InsertionSorter}. */
public abstract class LowMemoryMergeSorter extends AbstractMergeSorter {

  private final int maxTempSlots;

  /**
   * @param maxTempSlots maximum number of temporary slots that can be allocated
   */
  public LowMemoryMergeSorter(int maxTempSlots) {
    this.maxTempSlots = maxTempSlots;
  }

  @Override
  public final void sort(int from, int to) {
    checkRange(from, to);
    if (to - from <= maxTempSlots) {
      super.sort(from, to);
    } else {
      mergeSortInPlace(from, to);
    }
  }

}
