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

/** {@link Sorter} implementation based on the merge-sort algorithm that merges
 *  runs using extra memory. Small arrays are sorted with {@link InsertionSorter}. */
public abstract class MergeSorter extends AbstractMergeSorter {

  @Override
  public final void sort(int from, int to) {
    requireCapacity(to - from);
    super.sort(from, to);
  }

  /** Make sure that the temporary storage contains at least <code>n</code> entries. */
  public abstract void requireCapacity(int n);

}
