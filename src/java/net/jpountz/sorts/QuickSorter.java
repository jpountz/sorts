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

/** {@link Sorter} implementation using the quicksort algorithm. Small arrays
 *  are sorted using {@link InsertionSorter}. */
public abstract class QuickSorter extends Sorter {

  @Override
  public final void sort(int from, int to) {
    quicksort(from, to, (Integer.SIZE - Integer.numberOfLeadingZeros(to - from)) << 1);
  }

  @Override
  void mergeSortInPlaceAux(int from, int to) {
    if (to - from < THRESHOLD) {
      insertionSort(from, to);
    } else {
      mergeSortInPlace(from, to);
    }
  }

  void quicksort(int from, int to, int maxDepth) {
    if (--maxDepth == 0 || to - from < THRESHOLD) {
      mergeSortInPlace(from, to);
      return;
    }

    final int mid = (from + to) >>> 1;

    if (compare(from, mid) > 0) {
      swap(from, mid);
    }

    if (compare(mid, to - 1) > 0) {
      swap(mid, to - 1);
      if (compare(from, mid) > 0) {
        swap(from, mid);
      }
    }

    int left = from + 1;
    int right = to - 2;

    setPivot(mid);
    for (;;) {
      while (comparePivot(right) < 0) {
        --right;
      }

      while (left < right && comparePivot(left) >= 0) {
        ++left;
      }

      if (left < right) {
        swap(left, right);
        --right;
      } else {
        break;
      }
    }

    quicksort(from, left + 1, maxDepth);
    quicksort(left + 1, to, maxDepth);
  }

  /** Use the slot at <code>i</code> as a pivot. */
  public abstract void setPivot(int i);

  /** Compare the pivot with the slot at <code>i</code>. */
  public abstract int comparePivot(int i);
}
