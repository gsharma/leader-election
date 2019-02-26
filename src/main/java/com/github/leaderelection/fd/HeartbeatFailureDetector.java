package com.github.leaderelection.fd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.Id;
import com.github.leaderelection.Member;
import com.github.leaderelection.MemberGroup;
import com.github.leaderelection.MemberTransport;
import com.github.leaderelection.messages.HeartbeatRequest;
import com.github.leaderelection.messages.HeartbeatResponse;

/**
 * A simple heartbeat-based failure detector implementation.
 * 
 * @author gaurav
 */
public final class HeartbeatFailureDetector extends Thread implements FailureDetector {
  private static final Logger logger =
      LogManager.getLogger(HeartbeatFailureDetector.class.getSimpleName());

  private final ConcurrentMap<Id, BlockingQueue<HeartbeatResponse>> memberHeartbeats =
      new ConcurrentHashMap<>();
  private final MemberTransport transport;
  private final MemberGroup memberGroup;
  private final Id sourceMemberId;

  private long heartbeatIntervalMillis = 5_000L;

  private int retryThreshold = 5;

  public HeartbeatFailureDetector(final MemberTransport transport, final MemberGroup memberGroup,
      final Id sourceMemberId) {
    setName("failure-detector");
    setDaemon(true);
    this.transport = transport;
    this.memberGroup = memberGroup;
    this.sourceMemberId = sourceMemberId;

    final Member sourceMember = memberGroup.findMember(sourceMemberId);
    final List<Member> otherMembers = new ArrayList<>(memberGroup.allMembers());
    otherMembers.remove(sourceMember);
    for (final Member member : otherMembers) {
      memberHeartbeats.put(member.getId(), new LinkedBlockingQueue<>());
    }
  }

  @Override
  public long getPollIntervalMillis() {
    return heartbeatIntervalMillis;
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        final Member sourceMember = memberGroup.findMember(sourceMemberId);
        final HeartbeatRequest heartbeat =
            new HeartbeatRequest(sourceMemberId, sourceMember.currentEpoch());
        final List<Member> otherMembers = new ArrayList<>(memberGroup.allMembers());
        otherMembers.remove(sourceMember);
        for (final Member member : otherMembers) {
          HeartbeatResponse response = null;
          try {
            response = HeartbeatResponse.class.cast(transport.dispatchTo(member, heartbeat));
          } catch (Exception problem) {
          }
          if (response != null) {
            memberHeartbeats.get(member.getId()).add(response);
          }

          // TODO: compute the windowed heartbeat aggregation by member
        }
        sleep(heartbeatIntervalMillis);
      } catch (InterruptedException interrupted) {
      }
    }
  }

  @Override
  public Assessment getAssessment() {
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
    } catch (Exception problem) {
      // TODO
    }
    return true;
  }

}
