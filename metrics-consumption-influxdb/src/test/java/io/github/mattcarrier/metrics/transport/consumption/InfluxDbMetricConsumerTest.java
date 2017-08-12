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

package io.github.mattcarrier.metrics.transport.consumption;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

/**
 * Unit tests for {@link InfluxDbMetricConsumer}.
 *
 * @author mattcarrier
 * @since Aug 10, 2017
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest(InfluxDBFactory.class)
public class InfluxDbMetricConsumerTest {
  @Mock
  private InfluxDB influx;

  @Before
  public void setup() {
    mockStatic(InfluxDBFactory.class);
    when(InfluxDBFactory.connect(any(String.class), any(String.class), any(String.class))).thenReturn(influx);
    when(influx.enableBatch(anyInt(), anyInt(), any())).thenReturn(influx);
  }

  @Test
  public void noBatch() {
    new InfluxDbMetricConsumer.Builder().withIsBatchingEnabled(false).build();
    verify(influx, never()).enableBatch(anyInt(), anyInt(), any());
  }

  @Test
  public void batched() {
    new InfluxDbMetricConsumer.Builder().build();
    verify(influx).enableBatch(anyInt(), anyInt(), any());
  }

  @Test
  public void postConvert() {
    Point p = Point.measurement("point").addField("field", "value").build();
    assertEquals(p, new InfluxDbMetricConsumer.Builder().build().postConvert(p));
    verify(influx).write("metrics", "autogen", p);
  }
}
