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


/** {@link Sorter} implementation based on the
 *  <a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">TimSort</a>
 *  algorithm. This algorithms is especially good at sorting partially-sorted arrays.
 *  Small arrays are sorted with {@link BinarySorter}. */
public abstract class TimSorter extends AbstractTimSorter {

  protected TimSorter() {
    super();
  }

  @Override
  void doMerge(int lo, int mid, int hi) {
    if (mid - lo < hi - mid) {
      mergeLo(lo, mid, hi);
    } else {
      mergeHi(lo, mid, hi);
    }
  }

  void mergeLo(int lo, int mid, int hi) {
    assert compare(lo, mid) > 0;
    final int len1 = mid - lo;
    save(lo, len1);
    copy(mid, lo);
    int i = 0, j = mid + 1, dest = lo + 1;
    outer: for (;;) {
      for (int count = 0; count < MIN_GALLOP; ) {
        if (i >= len1 || j >= hi) {
          break outer;
        } else if (compareSaved(i, j) <= 0) {
          restore(i++, dest++);
          count = 0;
        } else {
          copy(j++, dest++);
          ++count;
        }
      }
      // galloping...
      final int next = lowerSaved3(j, hi, i);
      for (; j < next; ++dest) {
        copy(j++, dest);
      }
      restore(i++, dest++);
    }
    for (; i < len1; ++dest) {
      restore(i++, dest);
    }
    assert j == dest;
  }

  void mergeHi(int lo, int mid, int hi) {
    assert compare(mid - 1, hi - 1) > 0;
    final int len2 = hi - mid;
    save(mid, len2);
    copy(mid - 1, hi - 1);
    int i = mid - 2, j = len2 - 1, dest = hi - 2;
    outer: for (;;) {
      for (int count = 0; count < MIN_GALLOP; ) {
        if (i < lo || j < 0) {
          break outer;
        } else if (compareSaved(j, i) >= 0) {
          restore(j--, dest--);
          count = 0;
        } else {
          copy(i--, dest--);
          ++count;
        }
      }
      // galloping
      final int next = upperSaved3(lo, i + 1, j);
      while (i >= next) {
        copy(i--, dest--);
      }
      restore(j--, dest--);
    }
    for (; j >= 0; --dest) {
      restore(j--, dest);
    }
    assert i == dest;
  }

  int lowerSaved(int from, int to, int val) {
    int len = to - from;
    while (len > 0) {
      final int half = len >>> 1,
        mid = from + half;
      if (compareSaved(val, mid) > 0) {
        from = mid + 1;
        len = len - half -1;
      } else {
        len = half;
      }
    }
    return from;
  }

  int upperSaved(int from, int to, int val) {
    int len = to - from;
    while (len > 0) {
      final int half = len >>> 1,
        mid = from + half;
      if (compareSaved(val, mid) < 0) {
        len = half;
      } else {
        from = mid + 1;
        len = len - half -1;
      }
    }
    return from;
  }

  // faster than lowerSaved when val is at the beginning of [from:to[
  int lowerSaved3(int from, int to, int val) {
    int f = from, t = f + 1;
    while (t < to) {
      if (compareSaved(val, t) <= 0) {
        return lowerSaved(f, t, val);
      }
      final int delta = t - f;
      f = t;
      t += delta << 1;
    }
    return lowerSaved(f, to, val);
  }

  //faster than upperSaved when val is at the end of [from:to[
  int upperSaved3(int from, int to, int val) {
    int f = to - 1, t = to;
    while (f > from) {
      if (compareSaved(val, f) >= 0) {
        return upperSaved(f, t, val);
      }
      final int delta = t - f;
      t = f;
      f -= delta << 1;
    }
    return upperSaved(from, t, val);
  }

  /** Copy <code>src</code> to <code>dest</code>. */
  protected abstract void copy(int src, int dest);

  /** Save elements between slots <code>i</code> and <code>i+len</code>. */
  protected abstract void save(int i, int len);

  /** Restore element <code>j</code> from the temporary storage into slot <code>i</code>. */
  protected abstract void restore(int i, int j);

  /** Compare element <code>i</code> from temp storage with element
   *  <code>j</code> from the slice to sort. */
  protected abstract int compareSaved(int i, int j);

}
