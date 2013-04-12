package net.jpountz.sorts;

import java.util.Arrays;

/** {@link Sorter} implementation based on the
 *  <a href="http://svn.python.org/projects/python/trunk/Objects/listsort.txt">TimSort</a>
 *  algorithm. This algorithms is especially good at sorting partially-sorted arrays.
 *  Small arrays are sorted with {@link BinarySorter}. */
public abstract class TimSorter extends Sorter {

  static final int MINRUN = 32;
  static final int THRESHOLD = 64;
  static final int STACKSIZE = 40; // depends on MINRUN
  static final int MIN_GALLOP = 7;

  int minRun;
  int to;
  int stackSize;
  int[] runEnds;

  protected TimSorter() {
    runEnds = new int[1 + STACKSIZE];
  }

  /** Minimum run length for an array of length <code>length</code>. */
  static int minRun(int length) {
    assert length >= MINRUN;
    int n = length;
    int r = 0;
    while (n >= 64) {
      r |= n & 1;
      n >>>= 1;
    }
    final int minRun = n + r;
    assert minRun >= MINRUN && minRun <= THRESHOLD;
    return minRun;
  }

  int runLen(int i) {
    final int off = stackSize - i;
    return runEnds[off] - runEnds[off - 1];
  }

  int runBase(int i) {
    return runEnds[stackSize - i - 1];
  }

  int runEnd(int i) {
    return runEnds[stackSize - i];
  }

  void setRunEnd(int i, int runEnd) {
    runEnds[stackSize - i] = runEnd;
  }

  void pushRunLen(int len) {
    runEnds[stackSize + 1] = runEnds[stackSize] + len;
    ++stackSize;
  }

  /** Compute the length of the next run, make the run sorted and return its
   *  length. */
  int nextRun() {
    final int runBase = runEnd(0);
    assert runBase < to;
    if (runBase == to - 1) {
      return 1;
    }
    int o = runBase + 1;
    if (compare(runBase, runBase+1) > 0) {
      // run must be strictly descending
      while (o < to && compare(o - 1, o) > 0) {
        ++o;
      }
      if (o - runBase < minRun && o < to) {
        o = Math.min(to, runBase + minRun);
        binarySort(runBase, o);
      } else {
        reverse(runBase, o);
      }
    } else {
      // run must be non-descending
      while (o < to && compare(o - 1, o) <= 0) {
        ++o;
      }
      if (o - runBase < minRun && o < to) {
        o = Math.min(to, runBase + minRun);
        binarySort(runBase, o);
      } // else nothing to do, the run is already sorted
    }
    return o - runBase;
  }

  void reverse(int from, int to) {
    while (from < --to) {
      swap(from++, to);
    }
  }

  void ensureInvariants() {
    while (stackSize > 1) {
      final int runLen0 = runLen(0);
      final int runLen1 = runLen(1);

      if (stackSize > 2) {
        final int runLen2 = runLen(2);

        if (runLen2 <= runLen1 + runLen0) {
          // merge the smaller of 0 and 2 with 1
          if (runLen2 < runLen0) {
            mergeAt(1);
          } else {
            mergeAt(0);
          }
          continue;
        }
      }

      if (runLen1 <= runLen0) {
        mergeAt(0);
        continue;
      }

      break;
    }
  }

  void exhaustStack() {
    while (stackSize > 1) {
      mergeAt(0);
    }
  }

  void reset(int from, int to) {
    stackSize = 0;
    Arrays.fill(runEnds, 0);
    this.to = to;
    final int length = to - from;
    this.minRun = length <= THRESHOLD ? length : minRun(length);
  }

  void mergeAt(int n) {
    assert stackSize >= 2;
    merge(runBase(n + 1), runBase(n), runEnd(n));
    for (int j = n + 1; j > 0; --j) {
      setRunEnd(j, runEnd(j-1));
    }
    --stackSize;
  }

  void merge(int lo, int mid, int hi) {
    if (compare(mid - 1, mid) <= 0) {
      return;
    }
    lo = upper2(lo, mid, mid);
    hi = lower2(mid, hi, mid - 1);
    if (mid - lo <= hi - mid) {
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

  @Override
  public void sort(int from, int to) {
    checkRange(from, to);
    if (to - from <= 1) {
      return;
    }
    reset(from, to);
    do {
      ensureInvariants();
      pushRunLen(nextRun());
    } while (runEnd(0) < to);
    exhaustStack();
    assert runEnd(0) == to;
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
