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

/**
 * Default metric consumer and logs out metrics.
 *
 * @author mattcarrier
 * @since Aug 03, 2017
 */
public class DefaultMetricConsumer extends MetricConsumer<TransportableMetric> {
  @Override
  protected TransportableMetric convert(TransportableMetric metric) {
    return metric;
  }
}
