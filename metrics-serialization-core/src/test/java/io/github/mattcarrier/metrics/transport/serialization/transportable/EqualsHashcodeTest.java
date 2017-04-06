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
package io.github.mattcarrier.metrics.transport.serialization.transportable;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Equals and hashcode tests for {@link Transportable} objects.
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class EqualsHashcodeTest {
  @Test
  public void transportableCounterTest() {
    EqualsVerifier.forClass(TransportableCounter.class).withRedefinedSubclass(TransportableMeter.class)
        .suppress(Warning.NONFINAL_FIELDS).verify();
  }

  @Test
  public void transportableMeterTest() {
    EqualsVerifier.forClass(TransportableMeter.class).withRedefinedSuperclass().suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void transportableGaugeTest() {
    EqualsVerifier.forClass(TransportableGauge.class).suppress(Warning.NONFINAL_FIELDS).verify();
  }

  @Test
  public void transportableSnapshotTest() {
    EqualsVerifier.forClass(TransportableSnapshot.class).withOnlyTheseFields("values").suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void transportableMetricTest() {
    EqualsVerifier.forClass(TransportableMetric.class).suppress(Warning.NONFINAL_FIELDS).verify();
  }
}
