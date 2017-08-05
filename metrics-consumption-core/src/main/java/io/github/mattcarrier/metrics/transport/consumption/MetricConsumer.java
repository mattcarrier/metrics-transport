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

import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base metric consumer.  It converts the given TransportableMetric into a known object and performs an operation
 * on the converted object.
 *
 * @author mattcarrier
 * @since Aug 03, 2017
 */
public abstract class MetricConsumer<T> {
  private static final Logger log = LoggerFactory.getLogger(MetricConsumer.class);

  /**
   * Consumes the given metric.
   *
   * @param metric
   *     the metric
   * @return the converted metric
   */
  public T consume(TransportableMetric metric) {
    return postConvert(convert(metric));
  }

  /**
   * Converts the given metric into a relevant object to the consumer.
   *
   * @param metric
   *     the metric
   * @return the converted metric
   */
  protected abstract T convert(TransportableMetric metric);

  /**
   * Performs an operation on the converted metric.
   *
   * @param metric
   *     the converted metric
   * @return the converted metric
   */
  protected T postConvert(T metric) {
    log.info("Metric received [{}]", metric);
    return metric;
  }
}