package com.github.leaderelection.fd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;
import com.github.leaderelection.Member;
import com.github.leaderelection.MemberGroup;
import com.github.leaderelection.TCPTransport;
import com.github.leaderelection.messages.Response;
import com.github.leaderelection.messages.ResponseType;
import com.github.leaderelection.messages.SwimFDAckResponse;
import com.github.leaderelection.messages.SwimFDPingProbe;

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
        // no point wasting cycles with only self in the group
        if (memberGroup.allMembers().size() > 1) {
          // select a random member from the group other than the sourceMember
          final Member memberToProbe = selectRandomMember(sourceMember);

          // send a ping probe to selected member
          final SwimFDPingProbe pingProbe = new SwimFDPingProbe(sourceMemberId, epoch);

          Response response = null;
          try {
            response = transport.dispatchTo(memberToProbe, pingProbe);
          } catch (IOException problem) {
            // TODO: handle timeout
          }

          SwimFDAckResponse ackResponse = null;
          if (response != null && response.getType() == ResponseType.ACK) {
            ackResponse = (SwimFDAckResponse) response;
          }
        } else {
          logger.info("[{}] Failure detector waiting for other members to join group",
              sourceMemberId);
        }

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
    if (index == memberCount && index > 0) {
      --index;
    }
    logger.info("[{}] Selected member at index {} from {} members in group", sourceMemberId, index,
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
    logger.info("[{}] Starting failure detector for {}", sourceMemberId, sourceMember);
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
    logger.info("[{}] Stopping failure detector for {}", sourceMemberId, sourceMember);
    interrupt();
    return true;
  }

}
