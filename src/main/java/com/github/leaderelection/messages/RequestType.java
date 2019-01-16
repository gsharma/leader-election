package com.github.leaderelection.messages;

/**
 * All known response types.
 * 
 * @author gaurav
 */
public enum RequestType {
  HEARTBEAT,
  // election-related requests
  ELECTION, COORDINATOR,
  // failure-detection requests
  FD_PING, FD_PING_REQUEST,
  // membership change requests
  MEMBER_FAILED, MEMBER_JOINED;

}
