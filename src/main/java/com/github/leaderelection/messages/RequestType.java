package com.github.leaderelection.messages;

/**
 * All known response types.
 * 
 * @author gaurav
 */
public enum RequestType {
  HEARTBEAT(1),
  // election-related requests
  ELECTION(11), COORDINATOR(12),
  // failure-detection requests
  FD_PING(21), FD_PING_REQUEST(22), FD_FAILED(23);

  private int ordinal;

  private RequestType(int ordinal) {
    this.ordinal = ordinal;
  }

}
