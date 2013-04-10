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
    int l = 1; // length of the run
    if (compare(runBase, runBase+1) > 0) {
      // run must be strictly descending
      while (runBase + l < to && compare(runBase + l - 1, runBase + l) > 0) {
        ++l;
      }
      if (l < minRun && runBase + l < to) {
        l = Math.min(to - runBase, minRun);
        binarySort(runBase, runBase + l);
      } else {
        // revert
        for (int i = 0, halfL = l >>> 1; i < halfL; ++i) {
          swap(runBase + i, runBase + l - i - 1);
        }
      }
    } else {
      // run must be non-descending
      while (runBase + l < to && compare(runBase + l - 1, runBase + l) <= 0) {
        ++l;
      }
      if (l < minRun && runBase + l < to) {
        l = Math.min(to - runBase, minRun);
        binarySort(runBase, runBase + l);
      } // else nothing to do, the run is already sorted
    }
    return l;
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
    if (mid - to <= hi - mid) {
      mergeLo(lo, mid, hi);
    } else {
      mergeHi(lo, mid, hi);
    }
  }

  /** Copy <code>src</code> to <code>dest</code>. */
  protected void copy(int src, int dest) {
    // because of the way we use copy, swap is good too
    swap(src, dest);
  }

  /** Save elements between slots <code>i</code> and <code>i+len</code>. */
  protected abstract void save(int i, int len);

  /** Restore element <code>j</code> from the temporary storage into slot <code>i</code>. */
  protected abstract void restore(int i, int j);

  /** Compare elements at offsets <code>i</code> and <code>j</code> in the temporary
   *  storage similarly to {@link #compare(int, int)}. */
  protected abstract int compareSaved(int i, int j);

  void mergeLo(int lo, int mid, int hi) {
    final int len1 = mid - lo;
    save(lo, len1);
    int i = 0, j = mid, dest = lo;
    for (; i < len1 && j < hi; ++dest) {
      if (compareSaved(i, j) <= 0) {
        restore(i++, dest);
      } else {
        copy(j++, dest);
      }
    }
    for (; i < len1; ++dest) {
      restore(i++, dest);
    }
    assert j == dest;
  }

  void mergeHi(int lo, int mid, int hi) {
    final int len2 = hi - mid;
    save(mid, len2);
    int i = mid - 1, j = len2 - 1, dest = hi - 1;
    for (; i >= lo && j >= 0; --dest) {
      if (compareSaved(j, i) >= 0) {
        restore(j--, dest);
      } else {
        copy(i--, dest);
      }
    }
    for (; j >= 0; --dest) {
      restore(j--, dest);
    }
    assert i == dest;
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

}
