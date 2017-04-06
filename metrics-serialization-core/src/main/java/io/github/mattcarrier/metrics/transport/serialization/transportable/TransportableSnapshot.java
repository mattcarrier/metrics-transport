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

import java.util.Arrays;
import java.util.Objects;

import com.codahale.metrics.Snapshot;
import com.google.common.base.MoreObjects;

/**
 * Transportable {@link Snapshot}
 * 
 * @author mattcarrier
 * @since Apr 2, 2017
 */
public class TransportableSnapshot implements Transportable {
  private static final long serialVersionUID = 1L;

  private long[]            values;
  private int               size;
  private long              max;
  private double            mean;
  private long              min;
  private double            stdDev;
  private double            median;
  private double            seventyFifth;
  private double            ninetyFifth;
  private double            ninetyEigth;
  private double            ninetyNinth;
  private double            nineHundredNinetyNinth;

  public TransportableSnapshot() {

  }

  public TransportableSnapshot(long[] values, long max, double mean, long min, double stdDev, double median,
      double seventyFifth, double ninetyFifth, double ninetyEigth, double ninetyNinth, double nineHundredNinetyNinth) {
    this.values = values;
    this.size = values.length;
    this.max = max;
    this.mean = mean;
    this.min = min;
    this.stdDev = stdDev;
    this.median = median;
    this.seventyFifth = seventyFifth;
    this.ninetyFifth = ninetyFifth;
    this.ninetyEigth = ninetyEigth;
    this.ninetyNinth = ninetyNinth;
    this.nineHundredNinetyNinth = nineHundredNinetyNinth;
  }

  /**
   * Generates a new {@link TransportableSnapshot} from an originating
   * {@link Snapshot}
   * 
   * @param snapshot
   *          the originating {@link Snapshot}
   * @return the generated {@link TransportableSnapshot}
   */
  public static TransportableSnapshot of(Snapshot snapshot) {
    return new TransportableSnapshot(snapshot.getValues(), snapshot.getMax(), snapshot.getMean(), snapshot.getMin(),
        snapshot.getStdDev(), snapshot.getMedian(), snapshot.get75thPercentile(), snapshot.get95thPercentile(),
        snapshot.get98thPercentile(), snapshot.get99thPercentile(), snapshot.get999thPercentile());
  }

  public long[] getValues() {
    return values;
  }

  public int size() {
    return size;
  }

  public long getMax() {
    return max;
  }

  public double getMean() {
    return mean;
  }

  public long getMin() {
    return min;
  }

  public double getStdDev() {
    return stdDev;
  }

  public double getMedian() {
    return median;
  }

  public double get75thPercentile() {
    return seventyFifth;
  }

  public double get95thPercentile() {
    return ninetyFifth;
  }

  public double get98thPercentile() {
    return ninetyEigth;
  }

  public double get99thPercentile() {
    return ninetyNinth;
  }

  public double get999thPercentile() {
    return nineHundredNinetyNinth;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(Arrays.hashCode(values));
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TransportableSnapshot)) {
      return false;
    }

    final TransportableSnapshot that = (TransportableSnapshot) obj;
    return Arrays.equals(this.values, that.values);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("values", values).add("size", size).add("max", max).add("mean", mean)
        .add("min", min).add("stdDev", stdDev).add("median", median).add("seventyFifth", seventyFifth)
        .add("ninetyFifth", ninetyFifth).add("ninetyEigth", ninetyEigth).add("ninetyNinth", ninetyNinth)
        .add("nineHundredNinetyNinth", nineHundredNinetyNinth).toString();
  }
}
