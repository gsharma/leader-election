package com.github.leaderelection;

/**
 * Skeleton for a member server participating in leader election as part of a group of servers.
 * 
 * @author gaurav
 */
public final class Member {
  private final Id id;
  private final String host;
  private final int port;

  public Member(final Id id, final String host, final int port) {
    this.id = id;
    this.host = host;
    this.port = port;
  }

  public Id getId() {
    return id;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

}
