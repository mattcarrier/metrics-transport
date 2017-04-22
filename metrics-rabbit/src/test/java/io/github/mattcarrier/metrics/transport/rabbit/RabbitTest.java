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
package io.github.mattcarrier.metrics.transport.rabbit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.OutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.github.mattcarrier.metrics.transport.serialization.JavaSerializer;
import io.github.mattcarrier.metrics.transport.serialization.Serializer;
import io.github.mattcarrier.metrics.transport.serialization.transportable.Transportable;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableCounter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableGauge;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMeter;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableSnapshot;

/**
 * Integration tests with RabbitMQ.
 * 
 * @author mattcarrier
 * @since Apr 4, 2017
 */
public class RabbitTest {
  private static final String                 RABBIT_HOST = System.getProperty("RABBIT_HOST",
      RabbitMQRule.DEFAULT_HOST);

  // If you use docker-machine for docker for mac/windows then you will need to
  // set the RABBIT_HOST appropriately
  // private static final String RABBIT_HOST = "192.168.99.100";

  private static Serializer                   serializer  = new JavaSerializer();
  private static MetricRegistry               registry    = new MetricRegistry();
  private static String                       metricName  = name(RabbitTest.class, "metric");
  private static ImmutableMap<String, Object> metricMeta  = ImmutableMap.<String, Object>builder()
      .put("host", "127.0.0.1").build();

  @ClassRule
  public static RabbitMQRule                  rabbit      = new RabbitMQRule(RabbitMQRule.DEFAULT_USERNAME,
      RabbitMQRule.DEFAULT_PASSWORD, RABBIT_HOST, RabbitMQRule.DEFAULT_PORT, RabbitMQRule.DEFAULT_VHOST);

  @BeforeClass
  public static void reporter() throws Exception {
    RabbitReporter reporter = new RabbitReporter.Builder(registry).metricMeta(metricMeta)
        .build(new RabbitClient.Builder().host(RABBIT_HOST).durable(false).autoDelete(true).build());
    reporter.start(100, TimeUnit.MILLISECONDS);
  }

  @Before
  public void purge() {
    registry.remove(metricName);
    rabbit.purge();
  }

  @Test
  public void counter() throws Exception {
    registry.register(metricName, new Counter() {
      final AtomicInteger i = new AtomicInteger();

      @Override
      public long getCount() {
        return i.getAndIncrement();
      }
    });

    rabbit.expect(5);
    compare(rabbit.wait(Duration.ofSeconds(5)),
        ImmutableList.of(counter(0), counter(1), counter(2), counter(3), counter(4)));
  }

  @Test
  public void gauge() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    registry.register(metricName, (Gauge<Integer>) () -> i.getAndIncrement());

    rabbit.expect(5);
    compare(rabbit.wait(Duration.ofSeconds(5)), ImmutableList.of(gauge(0), gauge(1), gauge(2), gauge(3), gauge(4)));
  }

  @Test
  public void meter() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    final AtomicInteger counter = new AtomicInteger();
    registry.register(metricName, new Meter() {
      private int j;

      @Override
      public long getCount() {
        return val();
      }

      @Override
      public double getFifteenMinuteRate() {
        return val();
      }

      @Override
      public double getFiveMinuteRate() {
        return val();
      }

      @Override
      public double getMeanRate() {
        return val();
      }

      @Override
      public double getOneMinuteRate() {
        return val();
      }

      private int val() {
        if (0 == counter.getAndIncrement() % 5) {
          j = i.getAndIncrement();
        }

        return j;
      }
    });

    rabbit.expect(5);
    compare(rabbit.wait(Duration.ofSeconds(5)), ImmutableList.of(meter(0), meter(1), meter(2), meter(3), meter(4)));
  }

  @Test
  public void histogram() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    final AtomicInteger counter = new AtomicInteger();
    registry.register(metricName, new Histogram(new Reservoir() {
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
      private int j;

      @Override
      public Snapshot getSnapshot() {
        return new Snapshot() {
          @Override
          public double getValue(double quantile) {
            fail();
            return 0.0;
          }

          @Override
          public long[] getValues() {
            return new long[] { val() };
          }

          @Override
          public int size() {
            return val();
          }

          @Override
          public long getMax() {
            return val();
          }

          @Override
          public double getMean() {
            return val();
          }

          @Override
          public long getMin() {
            return val();
          }

          @Override
          public double getStdDev() {
            return val();
          }

          @Override
          public double getMedian() {
            return val();
          }

          @Override
          public double get75thPercentile() {
            return val();
          }

          @Override
          public double get95thPercentile() {
            return val();
          }

          @Override
          public double get98thPercentile() {
            return val();
          }

          @Override
          public double get99thPercentile() {
            return val();
          }

          @Override
          public double get999thPercentile() {
            return val();
          }

          @Override
          public void dump(OutputStream output) {
            fail();
          }
        };
      }

      private int val() {
        if (0 == counter.getAndIncrement() % 12) {
          j = i.getAndIncrement();
        }

        return j;
      }

      @Override
      public long getCount() {
        return val();
      }
    });

    rabbit.expect(5);
    compare(rabbit.wait(Duration.ofSeconds(5)),
        ImmutableList.of(histogram(0), histogram(1), histogram(2), histogram(3), histogram(4)));
  }

  @Test
  public void timer() throws Exception {
    final AtomicInteger i = new AtomicInteger();
    final AtomicInteger counter = new AtomicInteger();
    registry.register(metricName, new Timer() {
      private int j;

      @Override
      public Snapshot getSnapshot() {
        return new Snapshot() {
          @Override
          public double getValue(double quantile) {
            fail();
            return 0.0;
          }

          @Override
          public long[] getValues() {
            return new long[] { val() };
          }

          @Override
          public int size() {
            return val();
          }

          @Override
          public long getMax() {
            return val();
          }

          @Override
          public double getMean() {
            return val();
          }

          @Override
          public long getMin() {
            return val();
          }

          @Override
          public double getStdDev() {
            return val();
          }

          @Override
          public double getMedian() {
            return val();
          }

          @Override
          public double get75thPercentile() {
            return val();
          }

          @Override
          public double get95thPercentile() {
            return val();
          }

          @Override
          public double get98thPercentile() {
            return val();
          }

          @Override
          public double get99thPercentile() {
            return val();
          }

          @Override
          public double get999thPercentile() {
            return val();
          }

          @Override
          public void dump(OutputStream output) {
            fail();
          }
        };
      }

      private int val() {
        if (0 == counter.getAndIncrement() % 16) {
          j = i.getAndIncrement();
        }

        return j;
      }

      @Override
      public long getCount() {
        return val();
      }

      @Override
      public double getFifteenMinuteRate() {
        return val();
      }

      @Override
      public double getFiveMinuteRate() {
        return val();
      }

      @Override
      public double getMeanRate() {
        return val();
      }

      @Override
      public double getOneMinuteRate() {
        return val();
      }
    });

    rabbit.expect(5);
    compare(rabbit.wait(Duration.ofSeconds(5)), ImmutableList.of(timer(0), timer(1), timer(2), timer(3), timer(4)));
  }

  private Timer timer(int i) {
    return new Timer() {
      @Override
      public Snapshot getSnapshot() {
        return new Snapshot() {
          @Override
          public double getValue(double quantile) {
            fail();
            return 0.0;
          }

          @Override
          public long[] getValues() {
            return new long[] { i };
          }

          @Override
          public int size() {
            return i;
          }

          @Override
          public long getMax() {
            return i;
          }

          @Override
          public double getMean() {
            return i;
          }

          @Override
          public long getMin() {
            return i;
          }

          @Override
          public double getStdDev() {
            return i;
          }

          @Override
          public double getMedian() {
            return i;
          }

          @Override
          public double get75thPercentile() {
            return i;
          }

          @Override
          public double get95thPercentile() {
            return i;
          }

          @Override
          public double get98thPercentile() {
            return i;
          }

          @Override
          public double get99thPercentile() {
            return i;
          }

          @Override
          public double get999thPercentile() {
            return i;
          }

          @Override
          public void dump(OutputStream output) {
            fail();
          }
        };
      }

      @Override
      public long getCount() {
        return i;
      }

      @Override
      public double getFifteenMinuteRate() {
        return i;
      }

      @Override
      public double getFiveMinuteRate() {
        return i;
      }

      @Override
      public double getMeanRate() {
        return i;
      }

      @Override
      public double getOneMinuteRate() {
        return i;
      }
    };
  }

  private Histogram histogram(int i) {
    return new Histogram(new Reservoir() {
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
        return new Snapshot() {
          @Override
          public double getValue(double quantile) {
            fail();
            return 0.0;
          }

          @Override
          public long[] getValues() {
            return new long[] { i };
          }

          @Override
          public int size() {
            return i;
          }

          @Override
          public long getMax() {
            return i;
          }

          @Override
          public double getMean() {
            return i;
          }

          @Override
          public long getMin() {
            return i;
          }

          @Override
          public double getStdDev() {
            return i;
          }

          @Override
          public double getMedian() {
            return i;
          }

          @Override
          public double get75thPercentile() {
            return i;
          }

          @Override
          public double get95thPercentile() {
            return i;
          }

          @Override
          public double get98thPercentile() {
            return i;
          }

          @Override
          public double get99thPercentile() {
            return i;
          }

          @Override
          public double get999thPercentile() {
            return i;
          }

          @Override
          public void dump(OutputStream output) {
            fail();
          }
        };
      }

      @Override
      public long getCount() {
        return i;
      }
    };
  }

  private Meter meter(int i) {
    return new Meter() {
      @Override
      public long getCount() {
        return i;
      }

      @Override
      public double getFifteenMinuteRate() {
        return i;
      }

      @Override
      public double getFiveMinuteRate() {
        return i;
      }

      @Override
      public double getMeanRate() {
        return i;
      }

      @Override
      public double getOneMinuteRate() {
        return i;
      }
    };
  }

  private Counter counter(int i) {
    return new Counter() {
      @Override
      public long getCount() {
        return i;
      }
    };
  }

  private Gauge<Integer> gauge(int i) {
    return (Gauge<Integer>) () -> i;
  }

  private void compare(List<byte[]> actual, List<Metric> expected) throws Exception {
    int i = 0;
    for (byte[] body : actual) {
      TransportableMetric tm = serializer.deserialize(body);
      assertEquals(tm.getName(), metricName);
      assertNotNull(tm.getTimestamp());
      assertEquals(tm.getMeta(), metricMeta);
      compare(tm.getTransportables(), expected.get(i++));
    }
  }

  @SuppressWarnings("unchecked")
  private void compare(ImmutableSet<Transportable> actual, Metric expected) {
    for (Transportable t : actual) {
      if (expected instanceof Counter) {
        assertEquals(((Counter) expected).getCount(), ((TransportableCounter) t).getCount());
      } else if (expected instanceof Gauge) {
        assertEquals(((Gauge<Integer>) expected).getValue(), ((TransportableGauge<Integer>) t).getValue());
      } else if (expected instanceof Meter) {
        Meter expectedMeter = (Meter) expected;
        TransportableMeter actualMeter = (TransportableMeter) t;

        assertEquals(expectedMeter.getCount(), actualMeter.getCount());
        assertEquals(expectedMeter.getFifteenMinuteRate(), actualMeter.getFifteenMinuteRate(), 0.0);
        assertEquals(expectedMeter.getFiveMinuteRate(), actualMeter.getFiveMinuteRate(), 0.0);
        assertEquals(expectedMeter.getMeanRate(), actualMeter.getMeanRate(), 0.0);
        assertEquals(expectedMeter.getOneMinuteRate(), actualMeter.getOneMinuteRate(), 0.0);
      } else if (expected instanceof Histogram || (expected instanceof Timer && t instanceof TransportableSnapshot)) {
        if (t instanceof TransportableCounter) {
          assertEquals(((Histogram) expected).getCount(), ((TransportableCounter) t).getCount());
        } else if (t instanceof TransportableSnapshot) {
          Snapshot expectedSnapshot = ((Sampling) expected).getSnapshot();
          TransportableSnapshot actualSnapshot = (TransportableSnapshot) t;

          assertEquals(expectedSnapshot.get75thPercentile(), actualSnapshot.get75thPercentile(), 0.0);
          assertEquals(expectedSnapshot.get95thPercentile(), actualSnapshot.get95thPercentile(), 0.0);
          assertEquals(expectedSnapshot.get98thPercentile(), actualSnapshot.get98thPercentile(), 0.0);
          assertEquals(expectedSnapshot.get999thPercentile(), actualSnapshot.get999thPercentile(), 0.0);
          assertEquals(expectedSnapshot.get99thPercentile(), actualSnapshot.get99thPercentile(), 0.0);
          assertEquals(expectedSnapshot.getMax(), actualSnapshot.getMax());
          assertEquals(expectedSnapshot.getMean(), actualSnapshot.getMean(), 0.0);
          assertEquals(expectedSnapshot.getMedian(), actualSnapshot.getMedian(), 0.0);
          assertEquals(expectedSnapshot.getMin(), actualSnapshot.getMin());
          assertEquals(expectedSnapshot.getStdDev(), actualSnapshot.getStdDev(), 0.0);
          assertTrue(Arrays.equals(expectedSnapshot.getValues(), actualSnapshot.getValues()));
        } else {
          fail();
        }
      } else if (expected instanceof Timer) {
        if (t instanceof TransportableMeter) {
          Timer expectedTimer = (Timer) expected;
          TransportableMeter actualMeter = (TransportableMeter) t;

          assertEquals(expectedTimer.getCount(), actualMeter.getCount());
          assertEquals(expectedTimer.getFifteenMinuteRate(), actualMeter.getFifteenMinuteRate(), 0.0);
          assertEquals(expectedTimer.getFiveMinuteRate(), actualMeter.getFiveMinuteRate(), 0.0);
          assertEquals(expectedTimer.getMeanRate(), actualMeter.getMeanRate(), 0.0);
          assertEquals(expectedTimer.getOneMinuteRate(), actualMeter.getOneMinuteRate(), 0.0);
        } else {
          fail();
        }
      } else {
        fail();
      }
    }
  }
}
