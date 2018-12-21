package com.github.leaderelection;

/**
 * An enumeration of member's liveness status.
 * 
 * @author gaurav
 */
public enum Status {
  UNKNOWN(-9), ALIVE(0), DEAD(9);

  private final int status;

  private Status(final int status) {
    this.status = status;
  }

  public int statusNumber() {
    return status;
  }

}
