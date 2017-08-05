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

package io.github.mattcarrier.metrics.transport.consumer;

import static org.junit.Assert.assertEquals;

import io.github.mattcarrier.metrics.transport.consumption.MetricConsumer;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link io.github.mattcarrier.metrics.transport.consumption.MetricConsumer}.
 *
 * @author mattcarrier
 * @since Aug 05, 2017
 */
public class MetricConsumerTest {
  private MetricConsumer consumer;

  @Before
  public void create() {
    consumer = new MetricConsumer<Integer>() {
      int postConvertCounter = 0;

      @Override
      protected Integer convert(TransportableMetric metric) {
        return postConvertCounter;
      }

      @Override
      protected Integer postConvert(Integer postConvertCounter) {
        return this.postConvertCounter = postConvertCounter + 1;
      }
    };
  }

  @Test
  public void postConvert() {
    assertEquals(1, consumer.consume(null));
    assertEquals(2, consumer.consume(null));
  }
}
