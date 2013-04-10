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

import java.util.Comparator;

/** Base class for sort algorithm implementations. */
public abstract class Sorter {

  static final int THRESHOLD = 20;

  /** Compare entries found in slots <code>i</code> and <code>j</code>.
   *  The contract for the returned value is the same as
   *  {@link Comparator#compare(Object, Object)}. */
  protected abstract int compare(int i, int j);

  /** Swap slots <code>i</code> and <code>j</code>. */
  protected abstract void swap(int i, int j);

  /** Sort the slice which starts at <code>from</code> (inclusive) and ends at
   *  <code>to</code> (exclusive). */
  public abstract void sort(int from, int to);

  void checkRange(int from, int to) {
    if (to < from) {
      throw new IllegalArgumentException("'to' must be >= 'from', got from=" + from + " and to=" + to);
    }
  }

  void mergeInPlace(int from, int mid, int to) {
    if (from == mid || mid == to || compare(mid - 1, mid) <= 0) {
      return;
    } else if (to - from == 2) {
      swap(mid - 1, mid);
      return;
    }
    while (compare(from, mid) <= 0) {
      ++from;
    }
    while (compare(mid - 1, to - 1) <= 0) {
      --to;
    }
    int first_cut, second_cut;
    int len11, len22;
    if (mid - from > to - mid) {
      len11 = (mid - from) >>> 1;
      first_cut = from + len11;
      second_cut = lower(mid, to, first_cut);
      len22 = second_cut - mid;
    } else {
      len22 = (to - mid) >>> 1;
      second_cut = mid + len22;
      first_cut = upper(from, mid, second_cut);
      len11 = first_cut - from;
    }
    rotate( first_cut, mid, second_cut);
    final int new_mid = first_cut + len22;
    mergeInPlace(from, first_cut, new_mid);
    mergeInPlace(new_mid, second_cut, to);
  }

  int lower(int from, int to, int val) {
    int len = to - from;
    while (len > 0) {
      final int half = len >>> 1,
        mid = from + half;
      if (compare(mid, val) < 0) {
        from = mid + 1;
        len = len - half -1;
      } else {
        len = half;
      }
    }
    return from;
  }

  int upper(int from, int to, int val) {
    int len = to - from;
    while (len > 0) {
      final int half = len >>> 1,
        mid = from + half;
      if (compare(val, mid) < 0) {
        len = half;
      } else {
        from = mid + 1;
        len = len - half -1;
      }
    }
    return from;
  }

  void rotate(int lo, int mid, int hi) {
    int lot = lo;
    int hit = mid - 1;
    while (lot < hit) {
      swap(lot++, hit--);
    }
    lot = mid; hit = hi - 1;
    while (lot < hit) {
      swap(lot++, hit--);
    }
    lot = lo; hit = hi - 1;
    while (lot < hit) {
      swap(lot++, hit--);
    }
  }

  
  void mergeSortInPlaceAux(int from, int to) {
    sort(from, to);
  }

  void mergeSortInPlace(int from, int to) {
    final int mid = (from + to) >>> 1;
    mergeSortInPlaceAux(from, mid);
    mergeSortInPlaceAux(mid, to);
    mergeInPlace(from, mid, to);
  }

  void insertionSort(int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      for (int j = i; j > from; --j) {
        if (compare(j - 1, j) > 0) {
          swap(j - 1, j);
        } else {
          break;
        }
      }
    }
  }

  void binarySort(int from, int to) {
    for (int i = from + 1; i < to; ++i) {
      int l = from;
      int h = i - 1;
      while (l <= h) {
        final int mid = (l + h) >>> 1;
        final int cmp = compare(i, mid);
        if (cmp < 0) {
          h = mid - 1;
        } else {
          l = mid + 1;
        }
      }
      for (int j = i; j > l; --j) {
        swap(j - 1, j);
      }
    }
  }

  void heapSort(int from, int to) {
    if (to - from <= 1) {
      return;
    }
    heapify(from, to);
    for (int end = to - 1; end > from; --end) {
      swap(from, end);
      siftDown(from, from, end);
    }
  }

  void heapify(int from, int to) {
    for (int i = heapParent(from, to - 1); i >= from; --i) {
      siftDown(i, from, to);
    }
  }

  void siftDown(int i, int from, int to) {
    for (int leftChild = heapLeftChild(from, i); leftChild < to; leftChild = heapLeftChild(from, i)) {
      final int rightChild = leftChild + 1;
      if (compare(i, leftChild) < 0) {
        if (rightChild < to && compare(leftChild, rightChild) < 0) {
          swap(i, rightChild);
          i = rightChild;
        } else {
          swap(i, leftChild);
          i = leftChild;
        }
      } else if (rightChild < to && compare(i, rightChild) < 0) {
        swap(i, rightChild);
        i = rightChild;
      } else {
        break;
      }
    }
  }

  static int heapParent(int from, int i) {
    return (i - 1  + from) >>> 1;
  }

  static int heapLeftChild(int from, int i) {
    return (i << 1) + 1 - from;
  }

}
