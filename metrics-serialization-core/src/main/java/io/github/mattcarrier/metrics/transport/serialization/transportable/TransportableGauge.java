/**
 * Copyright 2017 Matt Carrier mcarrieruri@gmail.com
 *
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
package io.github.mattcarrier.metrics.transport.serialization.transportable;

import java.util.Objects;

import com.codahale.metrics.Gauge;
import com.google.common.base.MoreObjects;

/**
 * Transportable {@link Gauge}
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableGauge<T> implements Transportable, Gauge<T> {
  private static final long serialVersionUID = 1L;

  private T                 value;

  public TransportableGauge() {

  }

  public TransportableGauge(T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return value;
  }

  /**
   * Generates a new {@link TransportableGauge} from an originating
   * {@link Gauge}
   * 
   * @param gauge
   *          the originating {@link Gauge}
   * @return the generated {@link TransportableGauge}
   */
  public static <T> TransportableGauge<T> of(Gauge<T> gauge) {
    return new TransportableGauge<>(gauge.getValue());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(this.value);
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TransportableGauge)) {
      return false;
    }

    final TransportableGauge<?> that = (TransportableGauge<?>) obj;
    return Objects.equals(this.value, that.value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("value", this.value).toString();
  }
}
