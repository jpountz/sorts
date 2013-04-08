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

public class Entry implements java.lang.Comparable<Entry> {

  public final int value;
  public final int ord;

  public Entry(int value, int ord) {
    this.value = value;
    this.ord = ord;
  }

  @Override
  public int compareTo(Entry other) {
    return value < other.value ? -1 : value == other.value ? 0 : 1;
  }

}
