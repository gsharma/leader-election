package com.github.leaderelection;

import java.io.IOException;

/**
 * A heartbeat-based failure detector implementation.
 * 
 * @author gaurav
 */
public final class HeartbeatFailureDetector implements FailureDetector {
  private final TCPTransport transport;

  public HeartbeatFailureDetector(final TCPTransport transport) {
    this.transport = transport;
  }

  @Override
  public Assessment assess() {
    // TODO
    return null;
  }

  @Override
  public boolean init() {
    // TODO
    return false;
  }

  @Override
  public boolean tini() {
    // TODO
    try {
      transport.tini();
    } catch (IOException problem) {
      // TODO
    }
    return false;
  }

  @Override
  public boolean addMember(Member member) {
    // TODO
    return false;
  }

  @Override
  public boolean removeMember(Member member) {
    // TODO
    return false;
  }

}
