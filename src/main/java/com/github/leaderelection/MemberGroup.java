package com.github.leaderelection;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of members that together form a cluster. For the purposes
 * 
 * @author gaurav
 */
public final class MemberGroup {
  private final List<Member> members = new ArrayList<>();
  private final Id id;

  public MemberGroup(final Id id, final List<Member> members) {
    this.id = id;
    for (final Member member : members) {
      members.add(member);
    }
  }

  public boolean removeMember(final Member member) {
    return members.remove(member);
  }

  public boolean addMember(final Member member) {
    return members.add(member);
  }

  public Id getId() {
    return id;
  }

  public List<Member> largerMembers(final Member member) {
    final List<Member> largerMembers = new ArrayList<>();
    for (final Member otherMember : members) {
      if (member.compareTo(otherMember) < 0) {
        largerMembers.add(otherMember);
      }
    }
    return largerMembers;
  }

  public List<Member> smallerMembers(final Member member) {
    final List<Member> smallerMembers = new ArrayList<>();
    for (final Member otherMember : members) {
      if (member.compareTo(otherMember) > 0) {
        smallerMembers.add(otherMember);
      }
    }
    return smallerMembers;
  }

}
