# Sorts

Versatile sorting algorithms that allow for sorting any kind of read/write
random-access data-structure. The simplest implementations only require you to
override two methods:
 - compare(slot1, slot2),
 - swap(slot1, slot2).

Available sorting algorithms:
 - Introsort (improved quicksort),
 - Merge sort,
 - Tim sort (improved merge sort for partially-sorted data),
 - Heap sort, on both binary and ternary heaps.
