package com.github.leaderelection;

/**
 * All known response types.
 * 
 * @author gaurav
 */
public enum RequestType {
  HEARTBEAT, ELECTION, COORDINATOR, PING, PING_REQUEST, FAILED;
}
