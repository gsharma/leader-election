package com.github.leaderelection;

/**
 * Sent to announce leader election.
 * 
 * @author gaurav
 */
public final class ElectionRequest {
  private Id senderId;
  private Epoch epoch;
}
