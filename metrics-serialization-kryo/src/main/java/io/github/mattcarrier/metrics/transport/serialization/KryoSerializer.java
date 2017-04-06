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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;

/**
 * {@link Serializer} implementation utilizing
 * <a href="https://github.com/EsotericSoftware/kryo">Kryo</a> serialization.
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class KryoSerializer implements Serializer {
  private final Kryo kryo;

  public KryoSerializer() {
    kryo = new Kryo();
    kryo.register(TransportableMetric.class);
    ImmutableMapSerializer.registerSerializers(kryo);
    ImmutableSetSerializer.registerSerializers(kryo);
  }

  @Override
  public byte[] serialize(TransportableMetric metric) throws Exception {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(); final Output o = new Output(baos)) {
      kryo.writeObject(o, metric);
      o.flush();
      return baos.toByteArray();
    }
  }

  @Override
  public TransportableMetric deserialize(byte[] serialized) throws Exception {
    try (final ByteArrayInputStream bais = new ByteArrayInputStream(serialized); final Input i = new Input(bais)) {
      return (TransportableMetric) kryo.readObject(i, TransportableMetric.class);
    }
  }
}
