package com.github.leaderelection;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
  private final AtomicReference<Member> electedLeader = new AtomicReference<>();

  private final AtomicBoolean running = new AtomicBoolean();

  private Epoch epoch = new Epoch();

  public BullyLeaderElection(final MemberGroup memberGroup, final Member sourceMember) {
    this.memberGroup = memberGroup;
    this.sourceMember = sourceMember;
    this.transport = sourceMember.getTransport();
    running.compareAndSet(false, true);
  }

  @Override
  public Epoch reportEpoch() {
    return epoch.deepCopy();
  }

  @Override
  public Member reportLeader() {
    return electedLeader.get();
  }

  @Override
  public synchronized void electLeader() throws LeaderElectionException {
    logger.info("Starting a round of leader election at {}", epoch);

    Member leader = null;
    if (!running.get()) {
      logger.warn("Cannot elect a leader when leader election is shutdown");
      return;
    }

    // bump epoch
    incrementEpoch();

    // find all members with id greater than sourceMember
    final List<Member> greaterIdMembers = memberGroup.largerMembers(sourceMember);

    // sourceMember has the highest id - grab and announce leadership
    if (greaterIdMembers.isEmpty()) {
      if (sourceMember.getStatus() != MemberStatus.ALIVE) {
        logger.warn(
            "Failed round of leader election having encountered a non-alive leadership candidate: {}",
            sourceMember);
        return;
      }
      leader = sourceMember;
      while (this.epoch.after(leader.currentEpoch())) {
        leader.incrementEpoch();
      }
      memberGroup.setLeader(leader);

      final CoordinatorRequest victoryMessage = new CoordinatorRequest(sourceMember.getId(), epoch);

      for (final Member member : memberGroup.otherMembers(sourceMember)) {
        logger.info("Announcing leader:{} to {} at {}", leader.getId(), member.getId(), epoch);
        boolean successfulDispatch = false;
        for (int iter = 0; iter < 3; iter++) {
          try {
            Response response = transport.dispatchTo(member, victoryMessage);
            while (leader.currentEpoch().after(member.currentEpoch())) {
              member.incrementEpoch();
            }
            successfulDispatch = true;
            break;
          } catch (Exception problem) {
            logger.error("Problem encountered dispatching victory message to member:{}, iter:{}",
                member.getId(), iter, problem);
          }
        }
        if (!successfulDispatch) {
          logger.error("Failed to successfully dispatch victory message to member:{}",
              member.getId());
        }
      }
      electedLeader.set(leader);
      logger.info("Elected leader:{} at {}", leader.getId(), epoch);
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
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public synchronized void shutdown() {
    logger.info("Shutting down leader election at {}", epoch);
    if (running.compareAndSet(true, false)) {
      for (final Member member : memberGroup.allMembers()) {
        member.shutdown();
      }
    }
  }

  private void incrementEpoch() {
    long previousEpoch = epoch.getEpoch();
    epoch = epoch.increment();
    long currentEpoch = epoch.getEpoch();
    logger.info("Epoch incr:{}->{}", previousEpoch, currentEpoch);
  }

  @Override
  public void notifyMemberFailed(final Id failedMemberId) throws LeaderElectionException {
    logger.info("Received failure notification for memberId:{}", failedMemberId);
    electLeader();
  }

}
