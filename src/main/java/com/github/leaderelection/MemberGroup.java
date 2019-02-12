package com.github.leaderelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.messages.MemberFailedMessage;
import com.github.leaderelection.messages.Response;

/**
 * A group of members that together form a cluster. For the purposes
 * 
 * @author gaurav
 */
public final class MemberGroup {
  private static final Logger logger = LogManager.getLogger(MemberGroup.class.getSimpleName());

  private final ReentrantReadWriteLock groupLock = new ReentrantReadWriteLock(true);

  private final List<Member> members = new CopyOnWriteArrayList<>();
  private final Id id;
  private AtomicReference<Member> leader = new AtomicReference<>();

  public MemberGroup(final Id id) {
    this.id = id;
  }

  public boolean removeMember(final Member member) {
    boolean success = false;
    if (groupLock.writeLock().tryLock()) {
      try {
        if (members.contains(member)) {
          logger.info("Removing member {} from group {}", member.getId(), id);
          member.setStatus(MemberStatus.DEAD);
          final List<Member> allOtherMembers = new ArrayList<>(members);
          allOtherMembers.remove(member);
          // let the member failed use the failed candidate as the sender, too
          final MemberFailedMessage failed =
              new MemberFailedMessage(member.getId(), member.currentEpoch(), member.getId());
          for (final Member toRemoveFrom : allOtherMembers) {
            try {
              Response response = member.getTransport().dispatchTo(toRemoveFrom, failed);
            } catch (Exception problem) {
              logger.error("Problem encountered dispatching member failed message to member:{}",
                  toRemoveFrom.getId(), problem);
            }
          }
          success = true;
          logger.info("Successfully removed member {} from group {}", member.getId(), id);
        } else {
          logger.warn("Failed to locate removal candidate member {} in group {}", member.getId(),
              id);
        }
      } finally {
        groupLock.writeLock().unlock();
      }
    } else {
      logger.warn("Failed to acquire writeLock for removeMember({})", member.getId().getId());
    }
    // logger.info("Marked member {} DEAD in group {}, success:{}", member.getId(), id, success);
    return success;
    // return members.remove(member);
  }

  // TODO: should be done via broadcast to all members in the group
  // TODO: broadcast addMember and test with multi-threaded or multi-process test topology
  public boolean addMember(final Member member) {
    boolean success = false;
    if (groupLock.writeLock().tryLock()) {
      try {
        if (!members.contains(member)) {
          members.add(member);
          success = true;
        }
      } finally {
        groupLock.writeLock().unlock();
      }
    } else {
      logger.warn("Failed to acquire writeLock for addMember({})", member.getId().getId());
    }
    logger.info("Added member {} to group {}, success:{}", member.getId(), id, success);
    return success;
  }

  public boolean setLeader(final Member leader) {
    boolean success = false;
    if (members.contains(leader)) {
      this.leader.set(leader);
      success = true;
    }
    logger.info("Set leader {} in group {}, success:{}", leader.getId(), id, success);
    return success;
  }

  public Member getLeader() {
    return leader.get();
  }

  public Member findMember(final Id memberId) {
    Member member = null;
    // if (groupLock.readLock().tryLock()) {
    // try {
    for (final Member candidate : allMembers()) {
      if (candidate.getId().equals(memberId)) {
        member = candidate;
        break;
      }
    }
    if (member == null) {
      logger.warn("Failed to find member {} in group {}", memberId, id);
    } else {
      logger.info("Located member {} in group {}", memberId, id);
    }
    // } finally {
    // groupLock.readLock().unlock();
    // }
    // } else {
    // logger.warn("Failed to acquire readLock for findMember({}), writeLocked:{},
    // readLockCount:{}",
    // memberId.getId(), groupLock.isWriteLocked(), groupLock.getReadLockCount());
    // }
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
    if (groupLock.readLock().tryLock()) {
      try {
        for (final Member otherMember : allMembers()) {
          if (member.compareTo(otherMember) < 0) {
            largerMembers.add(otherMember);
          }
        }
      } finally {
        groupLock.readLock().unlock();
      }
    } else {
      logger.warn("Failed to acquire readLock for largerMembers({})", member.getId().getId());
    }
    return largerMembers;
  }

  public List<Member> smallerMembers(final Member member) {
    final List<Member> smallerMembers = new ArrayList<>();
    if (groupLock.readLock().tryLock()) {
      try {
        for (final Member otherMember : allMembers()) {
          if (member.compareTo(otherMember) > 0) {
            smallerMembers.add(otherMember);
          }
        }
      } finally {
        groupLock.readLock().unlock();
      }
    } else {
      logger.warn("Failed to acquire readLock for smallerMembers({})", member.getId().getId());
    }
    return smallerMembers;
  }

  public List<Member> otherMembers(final Member member) {
    final List<Member> otherMembers = new ArrayList<>(members);
    otherMembers.remove(member);
    return otherMembers;
  }

  public Member greatestIdMember() {
    Member greatestIdMember = null;
    if (groupLock.readLock().tryLock()) {
      try {
        Collections.sort(members, new Comparator<Member>() {
          @Override
          public int compare(Member one, Member two) {
            return one.compareTo(two);
          }
        });
        greatestIdMember = members.get(members.size() - 1);
      } finally {
        groupLock.readLock().unlock();
      }
    } else {
      logger.warn("Failed to acquire readLock for greatestIdMember");
    }
    return greatestIdMember;
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
