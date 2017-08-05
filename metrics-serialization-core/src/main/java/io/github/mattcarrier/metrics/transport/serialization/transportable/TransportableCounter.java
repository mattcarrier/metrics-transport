/**
 * Copyright 2017 Matt Carrier mcarrieruri@gmail.com
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mattcarrier.metrics.transport.serialization.transportable;

import com.codahale.metrics.Counting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import java.util.Objects;

/**
 * Transportable {@link Counting}.
 *
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableCounter implements Transportable, Counting {
  private static final long serialVersionUID = 1L;

  protected long count;

  public TransportableCounter() {

  }

  public TransportableCounter(long count) {
    this.count = count;
  }

  /**
   * Generates a new {@link TransportableCounter} from an originating
   * {@link Counting}.
   *
   * @param counting
   *     the originating {@link Counting}
   * @return the generated {@link TransportableCounter}
   */
  public static TransportableCounter of(Counting counting) {
    return new TransportableCounter(counting.getCount());
  }

  @Override
  public long getCount() {
    return count;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.count);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TransportableCounter)) {
      return false;
    }

    final TransportableCounter that = (TransportableCounter) obj;
    return that.canEqual(this) && Objects.equals(this.count, that.count);
  }

  public boolean canEqual(Object other) {
    return (other instanceof TransportableCounter);
  }

  @Override
  public String toString() {
    return toStringHelper(this).toString();
  }

  protected ToStringHelper toStringHelper(TransportableCounter transportable) {
    return MoreObjects.toStringHelper(this).add("count", this.count);
  }
}
