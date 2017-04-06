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
package io.github.mattcarrier.metrics.transport.serialization.transportable;

import java.util.Objects;

import com.codahale.metrics.Metered;

/**
 * Transportable {@link Metered}
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableMeter extends TransportableCounter implements Metered {
  private static final long serialVersionUID = 1L;

  private double            fifteenMinute;
  private double            fiveMinute;
  private double            mean;
  private double            oneMinute;

  public TransportableMeter() {

  }

  public TransportableMeter(long count, double fifteenMinute, double fiveMinute, double mean, double oneMinute) {
    super(count);
    this.fifteenMinute = fifteenMinute;
    this.fiveMinute = fiveMinute;
    this.mean = mean;
    this.oneMinute = oneMinute;
  }

  /**
   * Generates a new {@link TransportableMeter} from an originating
   * {@link Metered}
   * 
   * @param metered
   *          the originating {@link Metered}
   * @return the generated {@link TransportableMeter}
   */
  public static TransportableMeter of(Metered metered) {
    return new TransportableMeter(metered.getCount(), metered.getFifteenMinuteRate(), metered.getFiveMinuteRate(),
        metered.getMeanRate(), metered.getOneMinuteRate());
  }

  @Override
  public double getFifteenMinuteRate() {
    return fifteenMinute;
  }

  @Override
  public double getFiveMinuteRate() {
    return fiveMinute;
  }

  @Override
  public double getMeanRate() {
    return mean;
  }

  @Override
  public double getOneMinuteRate() {
    return oneMinute;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(super.hashCode(), this.fifteenMinute, this.fiveMinute, this.mean, this.oneMinute);
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!super.equals(obj) || !(obj instanceof TransportableMeter)) {
      return false;
    }

    final TransportableMeter that = (TransportableMeter) obj;
    return that.canEqual(this) && Objects.equals(this.fifteenMinute, that.fifteenMinute)
        && Objects.equals(this.fiveMinute, that.fiveMinute) && Objects.equals(this.mean, that.mean)
        && Objects.equals(this.oneMinute, that.oneMinute);
  }

  @Override
  public boolean canEqual(Object other) {
    return (other instanceof TransportableMeter);
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("fifteenMinute", fifteenMinute).add("fiveMinute", fiveMinute).add("mean", mean)
        .add("oneMinute", oneMinute).toString();
  }
}
