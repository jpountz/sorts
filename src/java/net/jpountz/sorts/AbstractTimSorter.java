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

import java.util.Arrays;

abstract class AbstractTimSorter extends Sorter {

  static final int MINRUN = 32;
  static final int THRESHOLD = 64;
  static final int STACKSIZE = 40; // depends on MINRUN
  static final int MIN_GALLOP = 7;

  int minRun;
  int to;
  int stackSize;
  int[] runEnds;

  protected AbstractTimSorter() {
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
    int o = runBase + 2;
    if (compare(runBase, runBase+1) > 0) {
      // run must be strictly descending
      while (o < to && compare(o - 1, o) > 0) {
        ++o;
      }
      reverse(runBase, o);
    } else {
      // run must be non-descending
      while (o < to && compare(o - 1, o) <= 0) {
        ++o;
      }
    }
    final int runHi = Math.max(o, Math.min(to, runBase + minRun));
    binarySort(runBase, runHi, o);
    return runHi - runBase;
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

  final void merge(int lo, int mid, int hi) {
    if (compare(mid - 1, mid) <= 0) {
      return;
    }
    lo = upper2(lo, mid, mid);
    hi = lower2(mid, hi, mid - 1);
    doMerge(lo, mid, hi);
  }

  abstract void doMerge(int lo, int mid, int hi);

  @Override
  public final void sort(int from, int to) {
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
