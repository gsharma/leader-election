package com.github.leaderelection.fd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;
import com.github.leaderelection.Member;
import com.github.leaderelection.MemberGroup;
import com.github.leaderelection.MemberStatus;
import com.github.leaderelection.MemberTransport;
import com.github.leaderelection.messages.MemberFailedMessage;
import com.github.leaderelection.messages.Response;
import com.github.leaderelection.messages.ResponseType;
import com.github.leaderelection.messages.SwimFDAckResponse;
import com.github.leaderelection.messages.SwimFDPingProbe;
import com.github.leaderelection.messages.SwimFDPingRequestProbe;

/**
 * A SWIM (Scalable Weakly-consistent Infection-style Process Group Membership) based failure
 * detector implementation.
 * 
 * @author gaurav
 */
public final class SwimFailureDetector extends Thread implements FailureDetector {
  private static final Logger logger =
      LogManager.getLogger(SwimFailureDetector.class.getSimpleName());

  private final AtomicReference<Assessment> assessmentRef = new AtomicReference<>();

  private final MemberTransport transport;
  private final MemberGroup memberGroup;
  private final Id sourceMemberId;
  private final Member sourceMember;

  private final Epoch epoch;

  // the protocol period needs to be at least 3x the RTT estimate
  private long protocolIntervalMillis = 3_000L;

  public SwimFailureDetector(final MemberTransport transport, final MemberGroup memberGroup,
      final Id sourceMemberId, final Epoch epoch) {
    setDaemon(true);
    this.transport = transport;
    this.memberGroup = memberGroup;
    this.sourceMemberId = sourceMemberId;
    this.epoch = epoch;
    this.sourceMember = memberGroup.findMember(sourceMemberId);
    setName("fd-" + sourceMember.getPort());
  }

  @Override
  public long getPollIntervalMillis() {
    return protocolIntervalMillis;
  }

  @Override
  public void run() {
    logger.info("Failure detector starting for {}:{} {}", sourceMember.getHost(),
        sourceMember.getPort(), sourceMemberId);
    final Map<Id, MemberStatus> memberStatuses = new HashMap<>();
    while (!isInterrupted() && transport.isRunning()) {
      final Assessment assessment = new Assessment(sourceMemberId, epoch, memberStatuses);
      memberStatuses.put(sourceMemberId, MemberStatus.ALIVE);
      try {
        // no point wasting cycles with only self in the group
        if (memberGroup.allMembers().size() > 1) {
          // select a random member from the group other than the sourceMember
          final Member memberToProbe = selectRandomMember(sourceMember);

          logger.info("Failure detector ready to dispatch ping probe to {}", memberToProbe.getId());

          // send a ping probe to selected member
          final SwimFDPingProbe pingProbe = new SwimFDPingProbe(sourceMemberId, epoch);

          Response response = null;
          try {
            response = transport.dispatchTo(memberToProbe, pingProbe);
          } catch (Exception problem) {
            // might have shutdown
            logger.error("Failure detector encountered error dispatching ping probe to {}",
                memberToProbe.getId(), problem);
          }

          SwimFDAckResponse ackResponse = null;
          if (response != null && response.getType() == ResponseType.ACK) {
            ackResponse = (SwimFDAckResponse) response;
          }

          // say we didn't receive the ackResponse and timed out
          if (ackResponse == null) {
            List<Member> proxyMembers = new ArrayList<>(memberGroup.allMembers());
            proxyMembers.remove(memberToProbe);
            proxyMembers.remove(sourceMember);

            // broadcast an indirect ping request probe to all (or k) other members to ping
            // memberToProbe on behalf of the sourceMember
            final List<Response> proxyResponses = new ArrayList<>(proxyMembers.size());
            for (final Member proxyMember : proxyMembers) {
              final SwimFDPingRequestProbe pingRequestProbe =
                  new SwimFDPingRequestProbe(sourceMemberId, epoch, memberToProbe.getId());
              for (int iter = 0; iter < 3; iter++) {
                logger.info("Failure detector dispatching ping-request probe to {}, iter {}",
                    memberToProbe.getId(), iter);
                Response proxyResponse = null;
                try {
                  proxyResponse = transport.dispatchTo(proxyMember, pingRequestProbe);
                  if (proxyResponse != null) {
                    proxyResponses.add(proxyResponse);
                    memberStatuses.put(memberToProbe.getId(), MemberStatus.ALIVE);
                    break;
                  } else {
                    memberStatuses.put(memberToProbe.getId(), MemberStatus.DEAD);
                    logger.warn(
                        "Failure detector failed in dispatching ping-request probe to {}, iter {}",
                        memberToProbe.getId(), iter);
                  }
                } catch (Exception problem) {
                  // TODO: handle timeout
                  logger.error(
                      "Failure detector encountered error dispatching ping-request probe to {}, iter {}",
                      proxyMember.getId(), iter, problem);
                }
              }
            }

            boolean receivedAck = false;
            for (final Response proxyResponse : proxyResponses) {
              SwimFDAckResponse proxyAckResponse = null;
              if (proxyResponse != null && proxyResponse.getType() == ResponseType.ACK) {
                proxyAckResponse = (SwimFDAckResponse) proxyResponse;
                // as soon as any one of the proxying members send back an ack response, the
                // sourceMember can again mark the memberToProbe as healthy and continue on
                receivedAck = true;
                break;
              }
            }

            if (receivedAck) {
              memberStatuses.put(memberToProbe.getId(), MemberStatus.ALIVE);
              logger.info("Failure detector successfully ping-request probed {}",
                  memberToProbe.getId());
            } else {
              logger.info("Failure detector failed to ping-request probe {}",
                  memberToProbe.getId());
              proxyMembers = new ArrayList<>(memberGroup.allMembers());
              proxyMembers.remove(memberToProbe);
              memberStatuses.put(memberToProbe.getId(), MemberStatus.DEAD);
              final MemberFailedMessage memberFailed = new MemberFailedMessage(sourceMember.getId(),
                  memberToProbe.currentEpoch(), memberToProbe.getId());
              for (final Member memberToNotify : proxyMembers) {
                try {
                  Response okResponse = transport.dispatchTo(memberToNotify, memberFailed);
                } catch (Exception problem) {
                  logger.error(
                      "Failure detector encountered error trying to dispatch failed-message to {}",
                      memberToNotify.getId(), problem);
                }
              }
            }
          } else {
            memberStatuses.put(memberToProbe.getId(), MemberStatus.ALIVE);
            logger.info("Failure detector successfully probed {}", memberToProbe.getId());
          }
        } else {
          logger.info("Failure detector waiting for other members to join group at {}",
              sourceMemberId);
        }

        logger.info("Failure detector {}", assessment);

        // hydrate assessment
        assessmentRef.set(assessment);

        sleep(protocolIntervalMillis);
      } catch (InterruptedException interrupted) {
        logger.info("Failure detector stopping for {}", sourceMemberId);
      }
    }
    logger.info("Failure detector stopped for {}", sourceMemberId);
  }

  private Member selectRandomMember(final Member sourceMember) {
    final List<Member> allMembers = new ArrayList<>(memberGroup.allMembers());
    allMembers.remove(sourceMember);
    int memberCount = allMembers.size();
    int index = (int) ((1 - Math.random()) * memberCount);
    if (index == memberCount && index > 0) {
      --index;
    }
    logger.info("Failure detector selected member at index {} from {} members at source member {}",
        index, memberGroup.allMembers().size(), sourceMemberId);
    return allMembers.get(index);
  }

  @Override
  public Assessment getAssessment() {
    return assessmentRef.get();
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
    interrupt();
    return true;
  }

}
