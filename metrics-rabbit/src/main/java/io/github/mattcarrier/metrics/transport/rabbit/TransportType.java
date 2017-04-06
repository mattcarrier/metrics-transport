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
package io.github.mattcarrier.metrics.transport.rabbit;

/**
 * All implemented transports and their corresponding types.
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public enum TransportType {
  TRANSPORTABLE_METRIC;

  private static final String classifier = "METRICS_RABBIT";
  private static final String delimiter  = "|";

  private final String        type;

  private TransportType() {
    this.type = classifier + delimiter + name();
  }

  public String getType() {
    return type;
  }

  /**
   * Retrieve the {@link TransportType} for the given type.
   * 
   * @param type
   *          the type
   * @return the corresponding {@link TransportType}
   */
  public static TransportType of(String type) {
    String name = getName(type);
    for (TransportType tt : values()) {
      if (tt.name().equals(name)) {
        return tt;
      }
    }

    return null;
  }

  private static String getName(String type) {
    return type.split(delimiter)[1];
  }
}
