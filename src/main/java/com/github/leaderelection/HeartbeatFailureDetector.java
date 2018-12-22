package com.github.leaderelection;

import java.io.IOException;

/**
 * A heartbeat-based failure detector implementation.
 * 
 * @author gaurav
 */
public final class HeartbeatFailureDetector implements FailureDetector {
  private final TCPTransport transport;
  private final MemberGroup memberGroup;

  public HeartbeatFailureDetector(final TCPTransport transport, final MemberGroup memberGroup) {
    this.transport = transport;
    this.memberGroup = memberGroup;
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
  public MemberGroup members() {
    return memberGroup;
  }

  @Override
  public boolean tini() {
    // TODO
    try {
      transport.shutdown();
    } catch (IOException problem) {
      // TODO
    }
    return false;
  }

}
