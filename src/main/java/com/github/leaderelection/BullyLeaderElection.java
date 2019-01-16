package com.github.leaderelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.messages.CoordinatorRequest;
import com.github.leaderelection.messages.ElectionRequest;
import com.github.leaderelection.messages.Response;

/**
 * An implementation of the bully leader election algorithm.
 * 
 * @author gaurav
 */
public final class BullyLeaderElection implements LeaderElection {
  private static final Logger logger =
      LogManager.getLogger(BullyLeaderElection.class.getSimpleName());

  private final MemberGroup memberGroup;
  private final MemberTransport transport;
  private final Member sourceMember;

  private final AtomicBoolean running = new AtomicBoolean();

  private Epoch epoch = new Epoch();

  public BullyLeaderElection(final MemberGroup memberGroup, final Member sourceMember) {
    this.memberGroup = memberGroup;
    this.sourceMember = sourceMember;
    this.transport = sourceMember.getTransport();
    running.compareAndSet(false, true);
  }

  @Override
  public synchronized Member electLeader() {
    logger.info("Starting a round of leader election at {}", epoch);

    Member leader = null;
    if (!running.get()) {
      logger.warn("Cannot elect a leader when leader election is shutdown");
      return leader;
    }

    // bump epoch
    incrementEpoch();

    // find all members with id greater than sourceMember
    final List<Member> greaterIdMembers = memberGroup.largerMembers(sourceMember);

    // sourceMember has the highest id - grab and announce leadership
    if (greaterIdMembers.isEmpty()) {
      leader = sourceMember;
      memberGroup.setLeader(leader);

      final CoordinatorRequest victoryMessage = new CoordinatorRequest(sourceMember.getId(), epoch);

      for (final Member member : otherMembers(memberGroup, sourceMember)) {
        logger.info("Announcing leader:{} to {} at {}", leader.getId(), member.getId(), epoch);
        while (leader.currentEpoch().after(member.currentEpoch())) {
          member.incrementEpoch();
        }
        try {
          Response response = transport.dispatchTo(member, victoryMessage);
        } catch (Exception problem) {
          logger.error("Problem encountered dispatching victory message to member:{}",
              member.getId(), problem);
        }
      }
    }
    // broadcast election to all greaterIdMembers
    else {
      final ElectionRequest electionRequest = new ElectionRequest(sourceMember.getId(), epoch);

      for (final Member greaterMember : greaterIdMembers) {
        logger.info("Member:{} sending election request to {} at {}", sourceMember.getId(),
            greaterMember.getId(), epoch);
        try {
          Response response = transport.dispatchTo(greaterMember, electionRequest);
        } catch (Exception problem) {
          logger.error("Problem encountered dispatching election rqequest to member:{}",
              greaterMember.getId(), problem);
        }
      }
    }

    logger.info("Elected leader:{} at {}", leader.getId(), epoch);
    return leader;
  }

  @Override
  public synchronized void shutdown() {
    if (running.compareAndSet(true, false)) {
      for (final Member member : memberGroup.allMembers()) {
        member.shutdown();
      }
    }
  }

  private static List<Member> otherMembers(final MemberGroup memberGroup,
      final Member sourceMember) {
    final List<Member> otherMembers = new ArrayList<>(memberGroup.allMembers());
    otherMembers.remove(sourceMember);
    return otherMembers;
  }

  private void incrementEpoch() {
    epoch = epoch.increment();
  }

  @Override
  public void notifyMemberFailed(final Id failedMemberId) {
    logger.info("Received failure notification for memberId:{}", failedMemberId);
    electLeader();
  }

}
