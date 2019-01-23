package com.github.leaderelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A group of members that together form a cluster. For the purposes
 * 
 * @author gaurav
 */
public final class MemberGroup {
  private static final Logger logger = LogManager.getLogger(MemberGroup.class.getSimpleName());

  private final List<Member> members = new ArrayList<>();
  private final Id id;
  private AtomicReference<Member> leader = new AtomicReference<>();

  public MemberGroup(final Id id) {
    this.id = id;
  }

  // TODO: should be done via broadcast to all members in the group
  public boolean removeMember(final Member member) {
    logger.info("Marking member {} DEAD in group {}", member.getId(), id);
    member.setStatus(MemberStatus.DEAD);
    return true;
    // return members.remove(member);
  }

  // TODO: should be done via broadcast to all members in the group
  public boolean addMember(final Member member) {
    logger.info("Adding member {} to group {}", member.getId(), id);
    return members.add(member);
  }

  public void setLeader(final Member leader) {
    logger.info("Setting leader {} in group {}", leader.getId(), id);
    this.leader.set(leader);
  }

  public Member getLeader() {
    return leader.get();
  }

  public Member findMember(final Id memberId) {
    Member member = null;
    for (final Member candidate : allMembers()) {
      if (candidate.getId().equals(memberId)) {
        member = candidate;
        break;
      }
    }
    if (member == null) {
      logger.warn("Failed to find member {} in group {}", memberId, id);
    }
    return member;
  }

  public Id getId() {
    return id;
  }

  public List<Member> allMembers() {
    return Collections.unmodifiableList(members);
  }

  public List<Member> largerMembers(final Member member) {
    final List<Member> largerMembers = new ArrayList<>();
    for (final Member otherMember : allMembers()) {
      if (member.compareTo(otherMember) < 0) {
        largerMembers.add(otherMember);
      }
    }
    return largerMembers;
  }

  public List<Member> smallerMembers(final Member member) {
    final List<Member> smallerMembers = new ArrayList<>();
    for (final Member otherMember : allMembers()) {
      if (member.compareTo(otherMember) > 0) {
        smallerMembers.add(otherMember);
      }
    }
    return smallerMembers;
  }

  public Member greatestIdMember() {
    Collections.sort(members, new Comparator<Member>() {
      @Override
      public int compare(Member one, Member two) {
        return one.compareTo(two);
      }
    });
    return members.get(members.size() - 1);
  }

  @Override
  public String toString() {
    final String leaderId = leader.get() != null ? leader.get().getId().toString() : null;
    final StringBuilder builder = new StringBuilder();
    builder.append("MemberGroup [id=").append(id).append(", leader=")
        .append(leader != null ? leaderId : "null").append(", members=").append(members)
        .append("]");
    return builder.toString();
  }

}
