package com.github.leaderelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A group of members that together form a cluster. For the purposes
 * 
 * @author gaurav
 */
public final class MemberGroup {
  private final List<Member> members = new ArrayList<>();
  private final Id id;

  public MemberGroup(final Id id) {
    this.id = id;
  }

  // TODO: should be done via broadcast to all members in the group
  public boolean removeMember(final Member member) {
    return members.remove(member);
  }

  // TODO: should be done via broadcast to all members in the group
  public boolean addMember(final Member member) {
    return members.add(member);
  }

  public Member findMember(final Id memberId) {
    Member member = null;
    for (final Member candidate : allMembers()) {
      if (candidate.getId().equals(memberId)) {
        member = candidate;
        break;
      }
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
    StringBuilder builder = new StringBuilder();
    builder.append("MemberGroup [members=").append(members).append(", ").append(id).append("]");
    return builder.toString();
  }

}
