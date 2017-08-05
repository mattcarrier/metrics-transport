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

package io.github.mattcarrier.metrics.transport.serialization;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides access to available serializers.
 *
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class SerializerFactory {
  private static final Logger log = LoggerFactory.getLogger(SerializerFactory.class);
  private final Serializer serializer;

  /**
   * Checks the classpath under package
   * 'io.github.mattcarrier.metrics.transport' for any existing serializers and
   * defaults to the {@link JavaSerializer} if none is found.
   *
   * @throws IOException
   *     if there is an issue scanning the classpath
   * @throws InstantiationException
   *     if there is an issue instantiating the serializer
   * @throws IllegalAccessException
   *     if there is an issue instantiating the serializer
   */
  public SerializerFactory() throws IOException, InstantiationException, IllegalAccessException {
    this("io.github.mattcarrier.metrics.transport");
  }

  /**
   * Checks the classpath under basePackage for any existing serializers and
   * defaults to the {@link JavaSerializer} if none is found.
   *
   * @param basePackage
   *     the base package to scan for serializers
   * @throws IOException
   *     if there is an issue scanning the classpath
   * @throws InstantiationException
   *     if there is an issue instantiating the serializer
   * @throws IllegalAccessException
   *     if there is an issue instantiating the serializer
   */
  public SerializerFactory(String basePackage) throws IOException, InstantiationException, IllegalAccessException {
    log.debug("Scanning the classpath under basePackage [{}] for metric serializers.", basePackage);
    final Set<Class<?>> serializers = new Reflections(basePackage).getTypesAnnotatedWith(SerializerImpl.class)
        .stream().filter(s -> JavaSerializer.class != s).collect(Collectors.toSet());

    if (serializers.isEmpty()) {
      log.warn("Using java serialization for metric transportation which is not suggested for production use.");
      serializer = new JavaSerializer();
      return;
    }

    if (1 != serializers.size()) {
      log.warn(
          "Multiple metric transportation serializer implementations have been found on the classpath [{}].",
          serializers
      );
    }

    final Class<?> serializerClass = serializers.iterator().next();
    log.info("Using [{}] for metric transportation serialization.", serializerClass);
    serializer = (Serializer) serializerClass.newInstance();
  }

  public Serializer serializer() {
    return serializer;
  }
}
