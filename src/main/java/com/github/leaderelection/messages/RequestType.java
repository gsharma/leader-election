package com.github.leaderelection.messages;

/**
 * All known response types.
 * 
 * @author gaurav
 */
public enum RequestType {
  HEARTBEAT, ELECTION, COORDINATOR, PING, PING_REQUEST, FAILED;
}
