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
import static org.junit.Assert.assertTrue;

import io.github.mattcarrier.metrics.transport.serialization.transportable.Transportable;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableCounter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableGauge;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMeter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableSnapshot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Integration Tests for {@link InfluxDbMetricConsumer}.
 *
 * @author mattcarrier
 * @since Aug 10, 2017
 */
public class InfluxDbMetricConsumerIntegrationTest {
  private static final String INFLUXDB_URL = "INFLUXDB_URL";

  private static InfluxDB influx;

  private static InfluxDbMetricConsumer consumer;

  @BeforeClass
  public static void setupClass() {
    influx = InfluxDBFactory.connect(System.getenv(INFLUXDB_URL), "root", "root");
    consumer =
        new InfluxDbMetricConsumer.Builder().withUrl(System.getenv(INFLUXDB_URL)).withIsBatchingEnabled(false).build();
  }

  @Before
  public void setup() {
    influx.deleteDatabase("metrics");
    influx.createDatabase("metrics");
  }

  @Test
  public void transportableMetric() {
    ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
    TransportableMetric m = buildAndConsumeMetric(now, new TransportableGauge<>(23.7));

    QueryResult.Series series = queryForMetric();
    assertEquals("metric", series.getName());
    assertEquals(DateTimeFormatter.ISO_DATE_TIME.format(m.getTimestamp()), series.getValues().get(0).get(0));
    series.getTags().forEach((k, v) -> {
      assertTrue(m.getMeta().containsKey(k));
      assertEquals(m.getMeta().get(k), v);
    });
  }

  @Test
  public void transportableGauge() {
    TransportableGauge<Double> gauge = new TransportableGauge<>(23.7);
    buildAndConsumeMetric(ZonedDateTime.now(Clock.systemUTC()), gauge);

    QueryResult.Series series = queryForMetric();
    assertEquals(gauge.getValue(), series.getValues().get(0).get(1));
  }

  @Test
  public void transportableCounter() {
    TransportableCounter counter = new TransportableCounter(5);
    buildAndConsumeMetric(ZonedDateTime.now(Clock.systemUTC()), counter);

    QueryResult.Series series = queryForMetric();
    assertEquals(Double.valueOf(counter.getCount()), series.getValues().get(0).get(1));
  }

  @Test
  public void transportableMeter() {
    TransportableMeter meter = new TransportableMeter(1, 2.0, 3.0, 4.0, 5.0);
    buildAndConsumeMetric(ZonedDateTime.now(Clock.systemUTC()), meter);

    Map<String, Object> results = mappedResults(queryForMetric());
    assertEquals(6, results.size());  // time is also included

    assertEquals(Double.valueOf(meter.getCount()), results.get("count"));
    assertEquals(meter.getOneMinuteRate(), results.get("oneMinuteRate"));
    assertEquals(meter.getFiveMinuteRate(), results.get("fiveMinuteRate"));
    assertEquals(meter.getFifteenMinuteRate(), results.get("fifteenMinuteRate"));
    assertEquals(meter.getMeanRate(), results.get("mean"));
  }

  @Test
  public void transportableSnapshot() {
    TransportableSnapshot snapshot = new TransportableSnapshot(new long[]{1, 2, 3}, 1, 2.0, 3, 4.0, 5.0,
                                                               6.0, 7.0, 8.0, 9.0,
                                                               10.0
    );
    buildAndConsumeMetric(ZonedDateTime.now(Clock.systemUTC()), snapshot);

    Map<String, Object> results = mappedResults(queryForMetric());
    assertEquals(11, results.size()); // time is included and values are not

    assertEquals(Double.valueOf(snapshot.getMax()), results.get("max"));
    assertEquals(snapshot.getMean(), results.get("mean"));
    assertEquals(Double.valueOf(snapshot.getMin()), results.get("min"));
    assertEquals(snapshot.getStdDev(), results.get("stdDev"));
    assertEquals(snapshot.getMedian(), results.get("median"));
    assertEquals(snapshot.get75thPercentile(), results.get("75thPercentile"));
    assertEquals(snapshot.get95thPercentile(), results.get("95thPercentile"));
    assertEquals(snapshot.get98thPercentile(), results.get("98thPercentile"));
    assertEquals(snapshot.get99thPercentile(), results.get("99thPercentile"));
    assertEquals(snapshot.get999thPercentile(), results.get("999thPercentile"));
  }

  private Map<String, Object> mappedResults(QueryResult.Series series) {
    return IntStream.range(0, series.getColumns().size()).boxed()
        .collect(Collectors.toMap(series.getColumns()::get, series.getValues().get(0)::get));
  }

  private TransportableMetric buildAndConsumeMetric(ZonedDateTime time, Transportable... transportables) {
    TransportableMetric m =
        new TransportableMetric("metric", time, ImmutableMap.of("tag1", "tag1value", "tag2", "tag2value"),
                                ImmutableSet.copyOf(transportables)
        );
    consumer.postConvert(consumer.convert(m));

    return m;
  }

  private QueryResult.Series queryForMetric() {
    QueryResult result = influx.query(new Query("SELECT * FROM metric GROUP BY tag1, tag2", "metrics"));
    assertEquals(1, result.getResults().size());

    List<QueryResult.Series> seriesList = result.getResults().get(0).getSeries();
    assertEquals(1, seriesList.size());

    return seriesList.get(0);
  }
}
