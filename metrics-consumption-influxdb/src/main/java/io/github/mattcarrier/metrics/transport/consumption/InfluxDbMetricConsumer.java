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

import io.github.mattcarrier.metrics.transport.serialization.transportable.Transportable;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableCounter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableGauge;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMeter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableSnapshot;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Metric consumer that converts {@link TransportableMetric}s to {@link Point}s and inserts them into influxdb.
 *
 * @author mattcarrier
 * @since Aug 05, 2017
 */
public class InfluxDbMetricConsumer extends MetricConsumer<Point> {
  private final InfluxDB influx;
  private final String   database;
  private final String   retentionPolicy;

  /**
   * Creates a fully initialized instance.
   */
  private InfluxDbMetricConsumer(Builder bldr) {
    InfluxDB influx = InfluxDBFactory.connect(bldr.url, bldr.username, bldr.password);
    this.influx = bldr.isBatchingEnabled ? influx
        .enableBatch(bldr.batchActions, bldr.flushDuration, bldr.flushDurationTimeUnit) : influx;
    this.database = bldr.database;
    this.retentionPolicy = bldr.retentionPolicy;
  }

  @Override
  protected Point postConvert(Point metric) {
    influx.write(database, retentionPolicy, metric);
    return metric;
  }

  @Override
  protected Point convert(TransportableMetric metric) {
    Point.Builder pointBuilder = Point.measurement(metric.getName());
    pointBuilder.time(metric.getTimestamp().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS);
    Map<String, String> tagMap = new HashMap<>();
    metric.getMeta().forEach((key, value) -> tagMap.put(key, value.toString()));
    pointBuilder.tag(tagMap);

    for (Transportable transportable : metric.getTransportables()) {
      if (transportable instanceof TransportableMeter) {
        addMeterFields(pointBuilder, (TransportableMeter) transportable);
      } else if (transportable instanceof TransportableSnapshot) {
        addSnapshotFields(pointBuilder, (TransportableSnapshot) transportable);
      } else if (transportable instanceof TransportableCounter) {
        addCounterFields(pointBuilder, (TransportableCounter) transportable);
      } else if (transportable instanceof TransportableGauge) {
        addGaugeFields(pointBuilder, (TransportableGauge<?>) transportable);
      }
    }

    return pointBuilder.build();
  }

  /**
   * Adds the {@link TransportableGauge} fields to the {@link Point.Builder}.
   *
   * @param pointBuilder
   *     the point builder
   * @param gauge
   *     the transportable gauge
   */
  private void addGaugeFields(Point.Builder pointBuilder, TransportableGauge<?> gauge) {
    pointBuilder.addField("value", Double.valueOf(String.valueOf(gauge.getValue())));
  }

  /**
   * Adds the {@link TransportableCounter} fields to the {@link Point.Builder}.
   *
   * @param pointBuilder
   *     the point builder
   * @param counter
   *     the transportable counter
   */
  private Point.Builder addCounterFields(Point.Builder pointBuilder, TransportableCounter counter) {
    return pointBuilder.addField("count", counter.getCount());
  }

  /**
   * Adds the {@link TransportableMeter} fields to the {@link Point.Builder}.
   *
   * @param pointBuilder
   *     the point builder
   * @param meter
   *     the transportable meter
   */
  private Point.Builder addMeterFields(Point.Builder pointBuilder, TransportableMeter meter) {
    return addCounterFields(pointBuilder, meter)
        .addField("oneMinuteRate", meter.getOneMinuteRate())
        .addField("fiveMinuteRate", meter.getFiveMinuteRate())
        .addField("fifteenMinuteRate", meter.getFifteenMinuteRate())
        .addField("mean", meter.getMeanRate());
  }

  /**
   * Adds the {@link TransportableSnapshot} fields to the {@link Point.Builder}.
   *
   * @param pointBuilder
   *     the point builder
   * @param snapshot
   *     the transportable snapshot
   */
  private Point.Builder addSnapshotFields(Point.Builder pointBuilder, TransportableSnapshot snapshot) {
    return pointBuilder
        .addField("max", snapshot.getMax())
        .addField("min", snapshot.getMin())
        .addField("median", snapshot.getMedian())
        .addField("mean", snapshot.getMean())
        .addField("stdDev", snapshot.getStdDev())
        .addField("75thPercentile", snapshot.get75thPercentile())
        .addField("95thPercentile", snapshot.get95thPercentile())
        .addField("98thPercentile", snapshot.get98thPercentile())
        .addField("99thPercentile", snapshot.get99thPercentile())
        .addField("999thPercentile", snapshot.get999thPercentile());
  }

  /**
   * Builds {@link InfluxDbMetricConsumer}s.
   *
   * @author mattcarrier
   * @since Aug 12, 2017
   */
  public static class Builder {
    private String   url                   = "http://localhost:8086";
    private String   username              = "root";
    private String   password              = "root";
    private boolean  isBatchingEnabled     = true;
    private int      batchActions          = 2000;
    private int      flushDuration         = 100;
    private TimeUnit flushDurationTimeUnit = TimeUnit.MILLISECONDS;
    private String   database              = "metrics";
    private String   retentionPolicy       = "autogen";

    public Builder withUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder withUsername(String username) {
      this.username = username;
      return this;
    }

    public Builder withPassword(String password) {
      this.password = password;
      return this;
    }

    public Builder withIsBatchingEnabled(boolean isBatchingEnabled) {
      this.isBatchingEnabled = isBatchingEnabled;
      return this;
    }

    public Builder withBatchActions(int batchActions) {
      this.batchActions = batchActions;
      return this;
    }

    public Builder withFlushDuration(int flushDuration) {
      this.flushDuration = flushDuration;
      return this;
    }

    public Builder withDatabase(String database) {
      this.database = database;
      return this;
    }

    public Builder withRetentionPolicy(String retentionPolicy) {
      this.retentionPolicy = retentionPolicy;
      return this;
    }

    /**
     * Builds an {@link InfluxDbMetricConsumer}.
     *
     * @return the newly created {@link InfluxDbMetricConsumer}
     */
    public InfluxDbMetricConsumer build() {
      return new InfluxDbMetricConsumer(this);
    }
  }
}
