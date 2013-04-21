package net.jpountz.sorts;

/** Variant of {@link TimSorter} with a configurable number of temporary slots
 *  to use. Even with a small number of temporary slots (1% for example), this
 *  {@link Sorter} should be significantly faster than {@link InPlaceTimSorter}. */
public abstract class LowMemoryTimSorter extends TimSorter {

  final int maxTempSlots;

  protected LowMemoryTimSorter(int maxTempSlots) {
    super();
    this.maxTempSlots = maxTempSlots;
  }

  @Override
  void doMerge(int lo, int mid, int hi) {
    if (hi - mid <= mid - lo && hi - mid <= maxTempSlots) {
      mergeHi(lo, mid, hi);
    } else if (mid - lo <= maxTempSlots) {
      mergeLo(lo, mid, hi);
    } else {
      mergeInPlace(lo, mid, hi);
    }
  }

  @Override
  void rotate(int lo, int mid, int hi) {
    final int len1 = mid - lo;
    final int len2 = hi - mid;
    if (len1 == len2) {
      while (mid < hi) {
        swap(lo++, mid++);
      }
    } else if (len2 < len1 && len2 <= maxTempSlots) {
      save(mid, len2);
      for (int i = lo + len1 - 1, j = hi - 1; i >= lo; --i, --j) {
        copy(i, j);
      }
      for (int i = 0, j = lo; i < len2; ++i, ++j) {
        restore(i, j);
      }
    } else if (len1 <= maxTempSlots) {
      save(lo, len1);
      for (int i = mid, j = lo; i < hi; ++i, ++j) {
        copy(i, j);
      }
      for (int i = 0, j = lo + len2; j < hi; ++i, ++j) {
        restore(i, j);
      }
    } else {
      reverse(lo, mid);
      reverse(mid, hi);
      reverse(lo, hi);
    }
  }

}
