package com.github.leaderelection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A SWIM-based failure detector implementation.
 * 
 * @author gaurav
 */
public final class SwimFailureDetector extends Thread implements FailureDetector {
  private static final Logger logger =
      LogManager.getLogger(SwimFailureDetector.class.getSimpleName());

  private final TCPTransport transport;
  private final MemberGroup memberGroup;
  private final Id sourceMemberId;
  private final Member sourceMember;

  private Epoch epoch;

  // the protocol period needs to be at least 3x the RTT estimate
  private long protocolIntervalMillis = 9_000L;

  public SwimFailureDetector(final TCPTransport transport, final MemberGroup memberGroup,
      final Id sourceMemberId) {
    setName("failure-detector");
    setDaemon(true);
    this.transport = transport;
    this.memberGroup = memberGroup;
    this.sourceMemberId = sourceMemberId;
    this.sourceMember = memberGroup.findMember(sourceMemberId);
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        // select a random member from the group other than the sourceMember
        final Member memberToProbe = selectRandomMember(sourceMember);

        // send a ping probe to selected member
        final SwimFDPingProbe pingProbe = new SwimFDPingProbe(sourceMemberId, epoch);

        // TODO 
        // Response response = transport.send(memberToProbe, pingProbe);
        // AckResponse ackResponse = null;
        // if (response.getType() == ResponseType.ACK) {
        //   ackResponse = (AckResponse) response;
        // }

        sleep(protocolIntervalMillis);
      } catch (InterruptedException interrupted) {
      }
    }
  }

  private Member selectRandomMember(final Member sourceMember) {
    final List<Member> allMembers = new ArrayList<>(memberGroup.allMembers());
    allMembers.remove(sourceMember);
    int memberCount = allMembers.size();
    int index = (int) ((1 - Math.random()) * memberCount);
    if (index == memberCount) {
      --index;
    }
    logger.info("Selected member at index {} from {} members in group", index,
        memberGroup.allMembers().size());
    return allMembers.get(index);
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
