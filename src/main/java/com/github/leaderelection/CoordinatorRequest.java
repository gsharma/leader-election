package com.github.leaderelection;

/**
 * Sent by the winner of leader election round to announce victory.
 * 
 * @author gaurav
 */
public final class CoordinatorRequest {
  private Id leaderId;
  private Epoch epoch;
}
