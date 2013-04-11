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

public class ArrayTimSorter<T extends java.lang.Comparable<? super T>> extends TimSorter {

  private final T[] arr;
  private T[] tmp;

  public ArrayTimSorter(T[] arr) {
    this.arr = arr;
  }

  @Override
  protected int compare(int i, int j) {
    return arr[i].compareTo(arr[j]);
  }

  @Override
  protected void swap(int i, int j) {
    final T tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
  }

  @Override
  protected void copy(int src, int dest) {
    arr[dest] = arr[src];
  }

  @Override
  protected void save(int start, int len) {
    if (tmp == null) {
      tmp = Arrays.copyOfRange(arr, start, start + len);
    } else {
      if (tmp.length < len) {
        int newLen = Math.max(len, tmp.length + (tmp.length >>> 1));
        if (newLen < 0) {
          newLen = Integer.MAX_VALUE;
        }
        @SuppressWarnings("unchecked")
        final T[] tmp = (T[]) new Comparable[newLen];
        this.tmp = tmp;
      }
      System.arraycopy(arr, start, tmp, 0, len);
    }
  }

  @Override
  protected void restore(int src, int dest) {
    arr[dest] = tmp[src];
  }

  @Override
  protected int compareSaved(int i, int j) {
    return tmp[i].compareTo(arr[j]);
  }

}
