/**
 * Copyright 2017 Matt Carrier
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
package io.github.mattcarrier.metrics.transport.serialization;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableCounter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableGauge;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMeter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableSnapshot;

/**
 * Tests serialization for {@link TransportableMetric} objects using a
 * {@link Serializer}
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public abstract class AbstractSerializerTest {
  private final Serializer serializer = serializer();

  /**
   * @return the {@link Serializer} to test
   */
  abstract protected Serializer serializer();

  @Test
  public void serializeTransportableCounter() throws Exception {
    final TransportableMetric metric = new TransportableMetric("name", ZonedDateTime.now(), ImmutableSet.of(counter()));
    assertEquals(metric, serializer.deserialize(serializer.serialize(metric)));
  }

  @Test
  public void serializeTransportableGauge() throws Exception {
    final TransportableMetric metric = new TransportableMetric("name", ZonedDateTime.now(), ImmutableSet.of(gauge()));
    assertEquals(metric, serializer.deserialize(serializer.serialize(metric)));
  }

  @Test
  public void serializeTransportableMeter() throws Exception {
    final TransportableMetric metric = new TransportableMetric("name", ZonedDateTime.now(), ImmutableSet.of(meter()));
    assertEquals(metric, serializer.deserialize(serializer.serialize(metric)));
  }

  @Test
  public void serializeTransportableSnapshot() throws Exception {
    final TransportableMetric metric = new TransportableMetric("name", ZonedDateTime.now(),
        ImmutableSet.of(snapshot()));
    assertEquals(metric, serializer.deserialize(serializer.serialize(metric)));
  }

  @Test
  public void serializeComposite() throws Exception {
    final TransportableMetric metric = new TransportableMetric("name", ZonedDateTime.now(),
        ImmutableSet.of(counter(), gauge(), meter(), snapshot()));
    assertEquals(metric, serializer.deserialize(serializer.serialize(metric)));
  }

  @Test
  public void serializeEmpty() throws Exception {
    final TransportableMetric metric = new TransportableMetric("name", ZonedDateTime.now(), ImmutableSet.of());
    assertEquals(metric, serializer.deserialize(serializer.serialize(metric)));
  }

  private TransportableCounter counter() {
    return new TransportableCounter(1);
  }

  private TransportableGauge<Integer> gauge() {
    return new TransportableGauge<Integer>(1);
  }

  private TransportableMeter meter() {
    return new TransportableMeter(1, 0.1, 0.2, 0.3, 0.4);
  }

  private TransportableSnapshot snapshot() {
    return new TransportableSnapshot(new long[] { 1 }, 2, 0.1, 3, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8);
  }
}
