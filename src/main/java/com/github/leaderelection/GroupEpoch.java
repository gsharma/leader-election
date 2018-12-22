package com.github.leaderelection;

/**
 * Model the cluster/group members' epoch.
 * 
 * @author gaurav
 */
public final class GroupEpoch implements Comparable<GroupEpoch> {
  private final long epoch;

  public GroupEpoch() {
    this(0L);
  }

  private GroupEpoch(final long epoch) {
    if (epoch < 0) {
      throw new IllegalArgumentException("Only positive epoch values are allowed");
    }
    this.epoch = epoch;
  }

  // due to the fact that ticking generates another immutable epoch by simply reading the
  // epoch, there's no need to lock here
  public GroupEpoch increment() {
    long nextEpoch = epoch;
    if (nextEpoch == Long.MAX_VALUE) {
      nextEpoch = 0L;
    } else {
      ++nextEpoch;
    }
    return new GroupEpoch(nextEpoch);
  }

  public boolean before(final GroupEpoch other) {
    return this.compareTo(other) < 0;
  }

  public boolean after(final GroupEpoch other) {
    return this.compareTo(other) > 0;
  }

  public long currentValue() {
    return epoch;
  }

  @Override
  public int compareTo(final GroupEpoch other) {
    return Long.compare(this.epoch, other.epoch);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Epoch[val:").append(epoch).append("]");
    return builder.toString();
  }

}
