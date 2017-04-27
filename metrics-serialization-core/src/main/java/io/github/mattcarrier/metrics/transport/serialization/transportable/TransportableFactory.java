/**
 * Copyright 2017 Matt Carrier mcarrieruri@gmail.com
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mattcarrier.metrics.transport.serialization.transportable;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Sampling;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Converts {@link Metric} objects into {@link Transportable} objects.
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableFactory {
  /**
   * Converts a {@link Metric} into {@link Transportable} objects.
   * 
   * @param name
   *          the name of the {@link Metric}
   * @param timestamp
   *          the creation timestamp
   * @param metric
   *          the {@link Metric} to convert
   * @return the {@link TransportableMetric}
   */
  public TransportableMetric convert(String name, ZonedDateTime timestamp, Metric metric) {
    return convert(name, timestamp, ImmutableMap.of(), metric);
  }

  /**
   * Converts a {@link Metric} into {@link Transportable} objects.
   * 
   * @param name
   *          the name of the {@link Metric}
   * @param timestamp
   *          the creation timestamp
   * @param meta
   *          additional metadata to attach to the {@link TransportableMetric}
   * @param metric
   *          the {@link Metric} to convert
   * @return the {@link TransportableMetric}
   */
  public TransportableMetric convert(String name, ZonedDateTime timestamp, Map<String, Object> meta, Metric metric) {
    ImmutableSet.Builder<Transportable> bldr = ImmutableSet.builder();

    if (Counting.class.isInstance(metric) && !Metered.class.isInstance(metric)) {
      bldr.add(TransportableCounter.of((Counting) metric));
    }

    if (Gauge.class.isInstance(metric)) {
      bldr.add(TransportableGauge.of((Gauge<?>) metric));
    }

    if (Metered.class.isInstance(metric)) {
      bldr.add(TransportableMeter.of((Metered) metric));
    }

    if (Sampling.class.isInstance(metric)) {
      bldr.add(TransportableSnapshot.of(((Sampling) metric).getSnapshot()));
    }

    return new TransportableMetric(name, timestamp, meta, bldr.build());
  }
}
