package com.github.leaderelection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.fd.FailureDetector;
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
  // private final FailureDetector failureDetector;
  private final MemberTransport transport;

  private final Member sourceMember;

  private Epoch epoch = new Epoch();

  public BullyLeaderElection(final MemberGroup memberGroup, final Member sourceMember,
      final MemberTransport transport) {
    this.memberGroup = memberGroup;
    this.sourceMember = sourceMember;
    this.transport = transport;
  }

  @Override
  public Member electLeader() {
    Member leader = null;

    // bump epoch
    incrementEpoch();

    // find all members with id greater than sourceMember
    final List<Member> greaterIdMembers = memberGroup.largerMembers(sourceMember);

    // sourceMember has the highest id - grab and announce leadership
    if (greaterIdMembers.isEmpty()) {
      leader = sourceMember;

      final CoordinatorRequest victoryMessage = new CoordinatorRequest(sourceMember.getId(), epoch);

      for (final Member member : otherMembers(memberGroup, sourceMember)) {
        logger.info("Announcing leader:{} to {} at {}", leader.getId(), member.getId(), epoch);
        try {
          Response response = transport.dispatchTo(member, victoryMessage);
        } catch (IOException problem) {
        }
      }
    }
    // broadcast election to all greaterIdMembers
    else {
      // TODO
      final ElectionRequest electionRequest = new ElectionRequest(sourceMember.getId(), epoch);

      for (final Member greaterMember : greaterIdMembers) {
        logger.info("Member:{} sending election request to {} at {}", sourceMember.getId(),
            greaterMember.getId(), epoch);
        try {
          Response response = transport.dispatchTo(greaterMember, electionRequest);
        } catch (IOException problem) {
        }
      }
    }

    logger.info("Elected leader:{} at {}", leader.getId(), epoch);

    return leader;
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

}
