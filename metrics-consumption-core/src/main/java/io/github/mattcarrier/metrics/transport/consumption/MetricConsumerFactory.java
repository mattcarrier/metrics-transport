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

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides access to available metric consumers.
 *
 * @author mattcarrier
 * @since Aug 03, 2017
 */
public class MetricConsumerFactory {
  private static final Logger log = LoggerFactory.getLogger(MetricConsumerFactory.class);
  private final MetricConsumer<?> consumer;

  /**
   * Checks the classpath under package
   * 'io.github.mattcarrier.metrics.transport' for any existing metric consumers and
   * defaults to the {@link DefaultMetricConsumer} if none is found.
   *
   * @throws IOException
   *     if there is an issue scanning the classpath
   * @throws InstantiationException
   *     if there is an issue instantiating the metric consumer
   * @throws IllegalAccessException
   *     if there is an issue instantiating the metric consumer
   */
  public MetricConsumerFactory() throws IOException, InstantiationException, IllegalAccessException {
    this("io.github.mattcarrier.metrics.transport");
  }

  /**
   * Checks the classpath under basePackage for any existing metric consumers and
   * defaults to the {@link DefaultMetricConsumer} if none is found.
   *
   * @param basePackage
   *     the base package to scan for metric consumers
   * @throws IOException
   *     if there is an issue scanning the classpath
   * @throws InstantiationException
   *     if there is an issue instantiating the metric consumer
   * @throws IllegalAccessException
   *     if there is an issue instantiating the metric consumer
   */
  public MetricConsumerFactory(String basePackage) throws IOException, InstantiationException, IllegalAccessException {
    log.debug("Scanning the classpath under basePackage [{}] for metric consumers.", basePackage);
    final Set<Class<?>> consumers =
        new Reflections(basePackage).getTypesAnnotatedWith(MetricConsumerImpl.class).stream()
            .filter(c -> DefaultMetricConsumer.class != c).collect(Collectors.toSet());

    if (consumers.isEmpty()) {
      log.warn("Using logging consumer for metric consumption.");
      consumer = new DefaultMetricConsumer();
      return;
    }

    if (1 != consumers.size()) {
      log.warn(
          "Multiple metric consumer implementations have been found on the classpath [{}].",
          consumers
      );
    }

    final Class<?> consumerClass = consumers.iterator().next();
    log.info("Using [{}] for metric consumption.", consumerClass);
    consumer = (MetricConsumer) consumerClass.newInstance();
  }

  public MetricConsumer<?> consumer() {
    return consumer;
  }
}
