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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import com.codahale.metrics.Metric;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Wrapper class that contains {@link Metric} metadata and the corresponding
 * metric data in {@link Transportable} objects.
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableMetric implements Serializable {
  private static final long            serialVersionUID = 1L;

  private String                       name;
  private ZonedDateTime                timestamp;
  private ImmutableMap<String, Object> meta;
  private ImmutableSet<Transportable>  transportables;

  public TransportableMetric() {

  }

  public TransportableMetric(String name, ZonedDateTime timestamp, ImmutableSet<Transportable> transportables) {
    this(name, timestamp, ImmutableMap.of(), transportables);
  }

  public TransportableMetric(String name, ZonedDateTime timestamp, Map<String, Object> meta,
      ImmutableSet<Transportable> transportables) {
    this.name = name;
    this.timestamp = timestamp;
    this.meta = ImmutableMap.copyOf(meta);
    this.transportables = transportables;
  }

  public String getName() {
    return name;
  }

  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  public ImmutableSet<Transportable> getTransportables() {
    return transportables;
  }

  public ImmutableMap<String, Object> getMeta() {
    return meta;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(this.meta, this.name, this.timestamp, this.transportables);
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TransportableMetric)) {
      return false;
    }

    final TransportableMetric that = (TransportableMetric) obj;
    return Objects.equals(this.meta, that.meta) && Objects.equals(this.name, that.name)
        && Objects.equals(this.timestamp, that.timestamp) && Objects.equals(this.transportables, that.transportables);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("meta", this.meta).add("name", this.name)
        .add("timestamp", this.timestamp).add("transportables", this.transportables).toString();
  }
}
