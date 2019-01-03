package com.github.leaderelection.fd;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.Id;
import com.github.leaderelection.Member;
import com.github.leaderelection.MemberGroup;
import com.github.leaderelection.TCPTransport;
import com.github.leaderelection.messages.HeartbeatRequest;

/**
 * A simple heartbeat-based failure detector implementation.
 * 
 * @author gaurav
 */
public final class HeartbeatFailureDetector extends Thread implements FailureDetector {
  private static final Logger logger =
      LogManager.getLogger(HeartbeatFailureDetector.class.getSimpleName());

  private final TCPTransport transport;
  private final MemberGroup memberGroup;
  private final Id sourceMemberId;

  private long heartbeatIntervalMillis = 5_000L;

  private int retryThreshold = 5;

  public HeartbeatFailureDetector(final TCPTransport transport, final MemberGroup memberGroup,
      final Id sourceMemberId) {
    setName("failure-detector");
    setDaemon(true);
    this.transport = transport;
    this.memberGroup = memberGroup;
    this.sourceMemberId = sourceMemberId;
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        final Member sourceMember = memberGroup.findMember(sourceMemberId);
        final HeartbeatRequest heartbeat =
            new HeartbeatRequest(sourceMemberId, sourceMember.currentEpoch());
        for (final Member member : memberGroup.allMembers()) {
        }

        sleep(heartbeatIntervalMillis);
      } catch (InterruptedException interrupted) {
      }
    }
  }

  @Override
  public Assessment assess() {
    // TODO
    return null;
  }

  @Override
  public boolean init() {
    start();
    return true;
  }

  @Override
  public MemberGroup members() {
    return memberGroup;
  }

  @Override
  public boolean tini() {
    // TODO
    try {
      interrupt();
      transport.shutdown();
    } catch (IOException problem) {
      // TODO
    }
    return true;
  }

}
