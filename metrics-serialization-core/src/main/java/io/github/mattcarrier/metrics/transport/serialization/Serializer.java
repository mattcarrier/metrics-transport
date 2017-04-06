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
package io.github.mattcarrier.metrics.transport.serialization;

import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;

/**
 * Serializes/Deserializes {@link TransportableMetric} objects.
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public interface Serializer {
  /**
   * Serializes the {@link TransportableMetric}.
   * 
   * @param metric
   *          the {@link TransportableMetric} to serialize
   * @return the serialized output
   * @throws Exception
   *           if there is an issue serializing the {@link TransportableMetric}
   */
  public byte[] serialize(TransportableMetric metric) throws Exception;

  /**
   * Deserializes the serialized input into a {@link TransportableMetric}.
   * 
   * @param serialized
   *          the serialized output
   * @return the deserialized {@link TransportableMetric}
   * @throws Exception
   *           if there is an issue deserializing the
   *           {@link TransportableMetric}
   */
  public TransportableMetric deserialize(byte[] serialized) throws Exception;
}
