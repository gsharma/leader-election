package com.github.leaderelection;

/**
 * An enumeration of member's liveness status.
 * 
 * @author gaurav
 */
public enum MemberStatus {
  UNKNOWN(-9), ALIVE(0), SUSPECTED_DEAD(9), DEAD(99);

  private final int status;

  private MemberStatus(final int status) {
    this.status = status;
  }

  public int statusNumber() {
    return status;
  }

}
