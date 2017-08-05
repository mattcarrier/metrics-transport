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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * {@link Transportable} conversion tests.
 *
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableFactoryTest {
  private final TransportableFactory tf = new TransportableFactory();

  @Test
  public void convertCounter() {
    final Counter counting = new Counter() {
      @Override
      public long getCount() {
        return 0;
      }
    };

    final String name = "name";
    final ZonedDateTime timestamp = ZonedDateTime.now();
    final TransportableMetric metric = tf.convert(name, timestamp, counting);
    final ImmutableSet<Transportable> transportables = metric.getTransportables();
    assertEquals(1, transportables.size());

    final TransportableCounter tc = (TransportableCounter) transportables.iterator().next();
    verifyTransportableMetricFields(name, timestamp, metric);
    verifyTransportableCounter(counting.getCount(), tc);
  }

  @Test
  public void convertGauge() {
    final Gauge<Integer> gauge = new Gauge<Integer>() {
      @Override
      public Integer getValue() {
        return 0;
      }
    };

    final String name = "name";
    final ZonedDateTime timestamp = ZonedDateTime.now();
    final TransportableMetric metric = tf.convert(name, timestamp, gauge);
    final ImmutableSet<Transportable> transportables = metric.getTransportables();
    assertEquals(1, transportables.size());

    @SuppressWarnings("unchecked") final TransportableGauge<Integer> tc =
        (TransportableGauge<Integer>) transportables.iterator().next();
    verifyTransportableMetricFields(name, timestamp, metric);
    verifyTransportableGauge(gauge.getValue(), tc);
  }

  @Test
  public void convertMeter() {
    final Meter metered = new Meter() {
      @Override
      public long getCount() {
        return 0;
      }

      @Override
      public double getFifteenMinuteRate() {
        return 1;
      }

      @Override
      public double getFiveMinuteRate() {
        return 2;
      }

      @Override
      public double getMeanRate() {
        return 3;
      }

      @Override
      public double getOneMinuteRate() {
        return 4;
      }
    };

    final String name = "name";
    final ZonedDateTime timestamp = ZonedDateTime.now();
    final TransportableMetric metric = tf.convert(name, timestamp, metered);
    final ImmutableSet<Transportable> transportables = metric.getTransportables();
    assertEquals(1, transportables.size());

    final TransportableMeter tm = (TransportableMeter) transportables.iterator().next();
    verifyTransportableMetricFields(name, timestamp, metric);
    verifyTransportableMeter(metered, tm);
  }

  @Test
  public void convertHistogram() {
    final Snapshot snapshot = snapshot();
    final Histogram histogram = new Histogram(new Reservoir() {
      @Override
      public void update(long value) {
        fail();
      }

      @Override
      public int size() {
        fail();
        return 0;
      }

      @Override
      public Snapshot getSnapshot() {
        fail();
        return null;
      }
    }) {
      @Override
      public Snapshot getSnapshot() {
        return snapshot;
      }

      @Override
      public long getCount() {
        return 0;
      }
    };

    final String name = "name";
    final ZonedDateTime timestamp = ZonedDateTime.now();
    final TransportableMetric metric = tf.convert(name, timestamp, histogram);
    final ImmutableSet<Transportable> transportables = metric.getTransportables();
    assertEquals(2, transportables.size());

    verifyTransportableMetricFields(name, timestamp, metric);

    for (Transportable t : transportables) {
      if (t instanceof TransportableSnapshot) {
        verifyTransportableSnapshot(histogram.getSnapshot(), (TransportableSnapshot) t);
      } else if (t instanceof TransportableCounter) {
        verifyTransportableCounter(histogram.getCount(), (TransportableCounter) t);
      }
    }
  }

  @Test
  public void convertTimer() {
    final Snapshot snapshot = snapshot();
    final Timer timer = new Timer() {
      @Override
      public Snapshot getSnapshot() {
        return snapshot;
      }

      @Override
      public long getCount() {
        return 0;
      }

      @Override
      public double getFifteenMinuteRate() {
        return 0.1;
      }

      @Override
      public double getFiveMinuteRate() {
        return 0.2;
      }

      @Override
      public double getMeanRate() {
        return 0.3;
      }

      @Override
      public double getOneMinuteRate() {
        return 0.4;
      }
    };

    final String name = "name";
    final ZonedDateTime timestamp = ZonedDateTime.now();
    final TransportableMetric metric = tf.convert(name, timestamp, timer);
    final ImmutableSet<Transportable> transportables = metric.getTransportables();
    assertEquals(2, transportables.size());

    verifyTransportableMetricFields(name, timestamp, metric);

    for (Transportable t : transportables) {
      if (t instanceof TransportableSnapshot) {
        verifyTransportableSnapshot(timer.getSnapshot(), (TransportableSnapshot) t);
      } else if (t instanceof TransportableMeter) {
        verifyTransportableMeter(timer, (TransportableMeter) t);
      }
    }
  }

  @Test
  public void convertMetricWithMeta() {
    final Counter counting = new Counter() {
      @Override
      public long getCount() {
        return 0;
      }
    };

    final String name = "name";
    final ZonedDateTime timestamp = ZonedDateTime.now();
    final ImmutableMap<String, Object> meta = ImmutableMap.<String, Object>builder().put("host", "127.0.0.1").build();
    final TransportableMetric metric = tf.convert(name, timestamp, meta, counting);
    final ImmutableSet<Transportable> transportables = metric.getTransportables();
    assertEquals(1, transportables.size());

    final TransportableCounter tc = (TransportableCounter) transportables.iterator().next();
    verifyTransportableMetricFields(name, timestamp, meta, metric);
    verifyTransportableCounter(counting.getCount(), tc);
  }

  private Snapshot snapshot() {
    return new Snapshot() {
      @Override
      public int size() {
        return 1;
      }

      @Override
      public long[] getValues() {
        return new long[]{1L};
      }

      @Override
      public double getValue(double quantile) {
        throw new RuntimeException("Should not be invoked");
      }

      @Override
      public double getStdDev() {
        return 0.1;
      }

      @Override
      public long getMin() {
        return 2;
      }

      @Override
      public double getMean() {
        return 0.2;
      }

      @Override
      public long getMax() {
        return 3;
      }

      @Override
      public double getMedian() {
        return 0.3;
      }

      @Override
      public double get75thPercentile() {
        return 0.4;
      }

      @Override
      public double get95thPercentile() {
        return 0.5;
      }

      @Override
      public double get98thPercentile() {
        return 0.6;
      }

      @Override
      public double get99thPercentile() {
        return 0.7;
      }

      @Override
      public double get999thPercentile() {
        return 0.8;
      }

      @Override
      public void dump(OutputStream output) {
        throw new RuntimeException("Should not be invoked");
      }
    };
  }

  private void verifyTransportableCounter(long count, TransportableCounter tc) {
    assertEquals(count, tc.getCount());
  }

  private <T> void verifyTransportableGauge(T value, TransportableGauge<T> tg) {
    assertEquals(value, tg.getValue());
  }

  private void verifyTransportableMeter(Metered metered, TransportableMeter tm) {
    assertEquals(metered.getCount(), tm.getCount());
    assertEquals(metered.getFifteenMinuteRate(), tm.getFifteenMinuteRate(), 0.0);
    assertEquals(metered.getFiveMinuteRate(), tm.getFiveMinuteRate(), 0.0);
    assertEquals(metered.getMeanRate(), tm.getMeanRate(), 0.0);
    assertEquals(metered.getOneMinuteRate(), tm.getOneMinuteRate(), 0.0);
  }

  private void verifyTransportableSnapshot(Snapshot snapshot, TransportableSnapshot ts) {
    assertEquals(snapshot.get75thPercentile(), ts.get75thPercentile(), 0.0);
    assertEquals(snapshot.get95thPercentile(), ts.get95thPercentile(), 0.0);
    assertEquals(snapshot.get98thPercentile(), ts.get98thPercentile(), 0.0);
    assertEquals(snapshot.get999thPercentile(), ts.get999thPercentile(), 0.0);
    assertEquals(snapshot.get99thPercentile(), ts.get99thPercentile(), 0.0);
    assertEquals(snapshot.getMax(), ts.getMax());
    assertEquals(snapshot.getMean(), ts.getMean(), 0.0);
    assertEquals(snapshot.getMedian(), ts.getMedian(), 0.0);
    assertEquals(snapshot.getMin(), ts.getMin());
    assertEquals(snapshot.getStdDev(), ts.getStdDev(), 0.0);
    assertTrue(Arrays.equals(snapshot.getValues(), ts.getValues()));
  }

  private void verifyTransportableMetricFields(String name, ZonedDateTime timestamp, TransportableMetric t) {
    verifyTransportableMetricFields(name, timestamp, ImmutableMap.of(), t);
  }

  private void verifyTransportableMetricFields(String name, ZonedDateTime timestamp, Map<String, Object> meta,
                                               TransportableMetric t) {
    assertEquals(name, t.getName());
    assertEquals(timestamp, t.getTimestamp());
    assertEquals(meta, t.getMeta());
  }
}
