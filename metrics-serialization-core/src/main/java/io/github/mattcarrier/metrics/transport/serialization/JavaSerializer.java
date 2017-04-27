/**
 * Copyright 2017 Matt Carrier mcarrieruri@gmail.com
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mattcarrier.metrics.transport.serialization;

import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Default {@link Serializer} implementation utilizing java serialization.
 *
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class JavaSerializer implements Serializer {
  @Override
  public byte[] serialize(TransportableMetric metric) throws Exception {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(metric);
      return baos.toByteArray();
    }
  }

  @Override
  public TransportableMetric deserialize(byte[] serialized) throws Exception {
    final ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
    final ObjectInputStream ois = new ObjectInputStream(bais);
    return (TransportableMetric) ois.readObject();
  }
}
