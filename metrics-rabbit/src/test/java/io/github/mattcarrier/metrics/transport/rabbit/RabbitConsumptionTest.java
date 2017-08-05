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

package io.github.mattcarrier.metrics.transport.rabbit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integration tests for rabbit consumption.
 *
 * @author mattcarrier
 * @since Aug 05, 2017
 */
public class RabbitConsumptionTest {
  private static final String RABBIT_HOST = System.getProperty(
      "RABBIT_HOST",
      RabbitMQRule.DEFAULT_HOST
  );
  private static final String metricName  = "metric";

  private static MetricRegistry               registry   = new MetricRegistry();
  private static ImmutableMap<String, Object> metricMeta = ImmutableMap.<String, Object>builder()
      .put("host", "127.0.0.1").build();

  private RabbitClient client;

  @BeforeClass
  public static void reporter() throws Exception {
    RabbitReporter reporter = new RabbitReporter.Builder(registry).metricMeta(metricMeta)
        .build(new RabbitClient.Builder().host(RABBIT_HOST).durable(false).autoDelete(true).build());
    reporter.start(100, TimeUnit.MILLISECONDS);
  }

  @Before
  public void buildClient() throws IllegalAccessException, NoSuchAlgorithmException, KeyManagementException,
      InstantiationException, TimeoutException, URISyntaxException, IOException {
    registry.remove(metricName);
    client = new RabbitClient.Builder().host(RABBIT_HOST).durable(false).autoDelete(true).build();
  }

  @After
  public void close() throws IOException, TimeoutException {
    registry.remove(metricName);
    client.close();
  }

  @Test
  public void consume() throws IOException, InterruptedException {
    client.consume("default");
    registry.register(metricName, new Counter() {
      final AtomicInteger i = new AtomicInteger();

      @Override
      public long getCount() {
        return i.getAndIncrement();
      }
    });

    Thread.sleep(1000L);
  }
}

