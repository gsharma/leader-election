package com.github.leaderelection;

/**
 * An OK Response is sent by non-leader candidate members participating in leader election as an
 * acknowledgement to give up on being a leader.
 * 
 * @author gaurav
 */
public final class OkResponse {
  private Id senderId;
  private Epoch epoch;
}
