package com.github.leaderelection;

/**
 * This is part of the (HeartbeatRequest:HeartbeatResponse) tuple.
 * 
 * @author gaurav
 */
public final class HeartbeatRequest {
  private Id senderId;
  private Epoch epoch;
}
