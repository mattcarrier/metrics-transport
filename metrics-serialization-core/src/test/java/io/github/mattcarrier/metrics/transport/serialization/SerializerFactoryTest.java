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
package io.github.mattcarrier.metrics.transport.serialization;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class SerializerFactoryTest {
  @Test
  public void defaultSerializer() throws InstantiationException, IllegalAccessException, IOException {
    assertEquals(JavaSerializer.class, new SerializerFactory().serializer().getClass());
  }
}