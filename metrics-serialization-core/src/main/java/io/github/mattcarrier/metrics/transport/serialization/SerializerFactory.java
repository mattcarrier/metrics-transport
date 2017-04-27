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

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides access to available serializers.
 *
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class SerializerFactory {
  private static final Logger log = LoggerFactory.getLogger(SerializerFactory.class);
  private final Serializer    serializer;

  /**
   * Checks the classpath for any existing serializers and defaults to the
   * {@link JavaSerializer} if none is found.
   *
   * @throws IOException
   *           if there is an issue scanning the classpath
   * @throws InstantiationException
   *           if there is an issue instantiating the serializer
   * @throws IllegalAccessException
   *           if there is an issue instantiating the serializer
   */
  public SerializerFactory() throws IOException, InstantiationException, IllegalAccessException {
    log.debug("Scanning the classpath for metric serializers.");
    final List<Class<?>> serializers = ClassPath.from(getClass().getClassLoader()).getAllClasses().stream()
        .filter(ci -> ci.getName().endsWith("Serializer")).map(ci -> ci.load())
        .filter(c -> Serializer.class.isAssignableFrom(c) && JavaSerializer.class != c && Serializer.class != c)
        .collect(Collectors.toList());

    if (serializers.isEmpty()) {
      log.warn("Using java serialization for metric transportation which is not suggested for production use.");
      serializer = new JavaSerializer();
      return;
    }

    if (1 != serializers.size()) {
      log.warn("Multiple metric transportation serializer implementations have been found on the classpath.");
    }

    final Class<?> serializerClass = serializers.get(0);
    log.info("Using [{}] for metric transportation serialization.", serializerClass.getSimpleName());
    serializer = (Serializer) serializerClass.newInstance();
  }

  public Serializer serializer() {
    return serializer;
  }
}
