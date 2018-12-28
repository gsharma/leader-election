package com.github.leaderelection;

/**
 * This is part of the (HeartbeatRequest:HeartbeatResponse) tuple.
 * 
 * @author gaurav
 */
public final class HeartbeatRequest {
  private final Id senderId;
  private final Epoch epoch;

  public HeartbeatRequest(final Id senderId, final Epoch epoch) {
    this.senderId = senderId;
    this.epoch = epoch;
  }

  public Id getSenderId() {
    return senderId;
  }

  public Epoch getEpoch() {
    return epoch;
  }

}
