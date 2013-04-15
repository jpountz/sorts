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
 * {@link Sorter} implementation based on a variant of the quicksort algorithm
 * called introsort: when the recursion level exceeds the log of the length of
 * the array to sort, it falls back to heapsort. This prevents quicksort from
 * running into its worst-case quadratic runtime. Small arrays are sorted with
 * {@link InsertionSorter}.
 */
public abstract class IntroSorter extends Sorter {

  static int ceilLog2(int n) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(n - 1);
  }

  @Override
  public final void sort(int from, int to) {
    checkRange(from, to);
    quicksort(from, to, ceilLog2(to - from));
  }

  void quicksort(int from, int to, int maxDepth) {
    if (to - from < THRESHOLD) {
      insertionSort(from, to);
      return;
    } else if (--maxDepth < 0) {
      heapSort(from, to);
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
  protected abstract void setPivot(int i);

  /** Compare the pivot with the slot at <code>i</code>. */
  protected abstract int comparePivot(int i);
}
