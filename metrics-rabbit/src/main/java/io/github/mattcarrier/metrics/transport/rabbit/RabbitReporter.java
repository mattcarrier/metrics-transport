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
package io.github.mattcarrier.metrics.transport.rabbit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;

import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableFactory;

/**
 * Publishes all metrics to RabbitMQ.
 * 
 * @author mattcarrier
 * @since Apr 4, 2017
 */
public class RabbitReporter extends ScheduledReporter {
  private final RabbitClient                 rabbit;
  private final Clock                        clock;
  private final TransportableFactory         factory;
  private final ImmutableMap<String, Object> metricMeta;

  protected RabbitReporter(MetricRegistry registry, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit,
      RabbitClient rabbit, Clock clock, TransportableFactory factory, Map<String, Object> metricMeta) {
    super(registry, "rabbit-reporter", filter, rateUnit, durationUnit);
    this.rabbit = rabbit;
    this.clock = clock;
    this.factory = factory;
    this.metricMeta = ImmutableMap.copyOf(metricMeta);
  }

  @Override
  public void report(@SuppressWarnings("rawtypes") SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
      SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
    final ZonedDateTime timestamp = ZonedDateTime.now(clock);

    gauges.forEach((k, m) -> rabbit.publish(factory.convert(k, timestamp, metricMeta, m)));
    counters.forEach((k, m) -> rabbit.publish(factory.convert(k, timestamp, metricMeta, m)));
    histograms.forEach((k, m) -> rabbit.publish(factory.convert(k, timestamp, metricMeta, m)));
    meters.forEach((k, m) -> rabbit.publish(factory.convert(k, timestamp, metricMeta, m)));
    timers.forEach((k, m) -> rabbit.publish(factory.convert(k, timestamp, metricMeta, m)));
  }

  /**
   * Builder for {@link RabbitReporter}
   * 
   * @author mattcarrier
   * @since Apr 4, 2017
   */
  public static class Builder {
    private final MetricRegistry registry;

    private MetricFilter         filter       = MetricFilter.ALL;
    private TimeUnit             rateUnit     = TimeUnit.SECONDS;
    private TimeUnit             durationUnit = TimeUnit.MILLISECONDS;
    private Clock                clock        = Clock.systemUTC();
    private TransportableFactory factory      = new TransportableFactory();
    private Map<String, Object>  metricMeta   = ImmutableMap.of();

    public Builder(MetricRegistry registry) {
      this.registry = registry;
    }

    public Builder filter(MetricFilter filter) {
      this.filter = filter;
      return this;
    }

    public Builder rateUnit(TimeUnit rateUnit) {
      this.rateUnit = rateUnit;
      return this;
    }

    public Builder durationUnit(TimeUnit durationUnit) {
      this.durationUnit = durationUnit;
      return this;
    }

    public Builder clock(Clock clock) {
      this.clock = clock;
      return this;
    }

    public Builder factory(TransportableFactory factory) {
      this.factory = factory;
      return this;
    }

    public Builder metricMeta(Map<String, Object> metricMeta) {
      this.metricMeta = metricMeta;
      return this;
    }

    /**
     * Builds the {@link RabbitReporter}
     * 
     * @param rabbit
     *          the {@link RabbitClient}
     * @return the {@link RabbitReporter}
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws URISyntaxException
     * @throws IOException
     * @throws TimeoutException
     */
    public RabbitReporter build(RabbitClient rabbit)
        throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
      return new RabbitReporter(registry, filter, rateUnit, durationUnit, rabbit, clock, factory, metricMeta);
    }
  }
}
