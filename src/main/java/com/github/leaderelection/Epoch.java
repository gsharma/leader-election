package com.github.leaderelection;

import java.io.Serializable;

/**
 * Model the cluster/group members' epoch.
 * 
 * @author gaurav
 */
public final class Epoch implements Serializable, Comparable<Epoch> {
  private static final long serialVersionUID = 1L;

  private final long epoch;

  public Epoch() {
    this(0L);
  }

  private Epoch(final long epoch) {
    if (epoch < 0) {
      throw new IllegalArgumentException("Only positive epoch values are allowed");
    }
    this.epoch = epoch;
  }

  // due to the fact that ticking generates another immutable epoch by simply reading the
  // epoch, there's no need to lock here
  public Epoch increment() {
    long nextEpoch = epoch;
    if (nextEpoch == Long.MAX_VALUE) {
      nextEpoch = 0L;
    } else {
      ++nextEpoch;
    }
    return new Epoch(nextEpoch);
  }

  public boolean before(final Epoch other) {
    return this.compareTo(other) < 0;
  }

  public boolean after(final Epoch other) {
    return this.compareTo(other) > 0;
  }

  public long getEpoch() {
    return epoch;
  }

  public Epoch deepCopy() {
    return new Epoch(epoch);
  }

  @Override
  public int compareTo(final Epoch other) {
    return Long.compare(this.epoch, other.epoch);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Epoch[val:").append(epoch).append("]");
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (epoch ^ (epoch >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Epoch)) {
      return false;
    }
    Epoch other = (Epoch) obj;
    if (epoch != other.epoch) {
      return false;
    }
    return true;
  }

}
