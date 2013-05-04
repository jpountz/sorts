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

public class ArrayMergeSorter<T extends java.lang.Comparable<? super T>> extends MergeSorter {

  private final T[] arr;
  private T[] savedSlots;

  public ArrayMergeSorter(T[] arr) {
    this.arr = arr;
  }

  @Override
  protected int compare(int i, int j) {
    return arr[i].compareTo(arr[j]);
  }

  @Override
  protected void save(int from, int to) {
    savedSlots[to] = arr[from];
  }

  @Override
  protected void restore(int i, int slot) {
    arr[slot] = savedSlots[i];
  }

  @Override
  protected int compareSaved(int i, int j) {
    return savedSlots[i].compareTo(savedSlots[j]);
  }

  @Override
  protected void requireCapacity(int n) {
    if (savedSlots == null || savedSlots.length < n) {
      @SuppressWarnings("unchecked")
      final T[] slots = (T[]) new java.lang.Comparable[n];
      savedSlots = slots;
    }
  }

  @Override
  protected void swap(int i, int j) {
    swap(arr, i, j);
  }

}
