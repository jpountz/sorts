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
 *  runs using extra memory. This implementation requires
 *  <code>array.length</code> temporary slots. Small arrays are sorted with
 *  {@link InsertionSorter}. */
public abstract class MergeSorter extends Sorter {

  @Override
  public final void sort(int from, int to) {
    checkRange(from, to);
    requireCapacity(to - from);
    mergeSort(from, to, from);
  }

  void mergeSort(int from, int to, int base) {
    if (to - from < THRESHOLD) {
      insertionSort(from, to);
      return;
    }
    final int mid = (from + to) >>> 1;
    final int q1 = (from + mid) >>> 1;
    final int q3 = (mid + to) >>> 1;
    mergeSort(from, q1, base);
    mergeSort(q1, mid, base);
    if (compare(q1 - 1, q1) <= 0) {
      mergeSort(mid, q3, base);
      mergeSort(q3, to, base);
      if (compare(q3 - 1, q3) <= 0) {
        if (compare(mid - 1, mid) > 0) {
          saveAll(from, to, base);
          merge2(from, mid, to, base);
        } // else nothing to do
      } else {
        merge1(mid, q3, to, base);
        saveAll(from, mid, base);
        merge2(from, mid, to, base);
      }
    } else {
      merge1(from, q1, mid, base);
      mergeSort(mid, q3, base);
      mergeSort(q3, to, base);
      if (compare(q3 - 1, q3) <= 0) {
        saveAll(mid, to, base);
      } else {
        merge1(mid, q3, to, base);
      }
      merge2(from, mid, to, base);
    }
  }

  void saveAll(int from, int to, int base) {
    for (int i = from; i < to; ++i) {
      save(i, i - base);
    }
  }

  void merge1(int from, int mid, int to, int base) {
    int dest = from - base, i = from, j = mid;
    for ( ; i < mid && j < to; ++dest) {
      if (compare(i, j) <= 0) {
        save(i++, dest);
      } else {
        save(j++, dest);
      }
    }
    for ( ; i < mid; ++i, ++dest) {
      save(i, dest);
    }
    for ( ; j < to; ++j, ++dest) {
      save(j, dest);
    }
    assert dest == to - base;
  }

  void merge2(int from, int mid, int to, int base) {
    if (compareSaved(mid - 1 - base, mid - base) <= 0) {
      for (int i = from; i < to; ++i) {
        restore(i - base, i);
      }
      return;
    }
    final int iend = mid - base, jend = to - base;
    int dest = from, i = from - base, j = mid - base;
    for ( ; i < iend && j < jend; ++dest) {
      if (compareSaved(i, j) <= 0) {
        restore(i++, dest);
      } else {
        restore(j++, dest);
      }
    }
    for ( ; i < iend; ++i, ++dest) {
      restore(i, dest);
    }
    for ( ; j < jend; ++j, ++dest) {
      restore(j, dest);
    }
    assert dest == to;
  }

  /** Save element in slot <code>i</code> in a temporary storage at offset<code>j</code>. */
  protected abstract void save(int i, int j);

  /** Restore element <code>j</code> from the temporary storage into slot <code>i</code>. */
  protected abstract void restore(int i, int j);

  /** Compare elements at offsets <code>i</code> and <code>j</code> in the temporary
   *  storage similarly to {@link #compare(int, int)}. */
  protected abstract int compareSaved(int i, int j);

  /** Make sure that the temporary storage contains at least <code>n</code> entries. */
  protected abstract void requireCapacity(int n);

}
