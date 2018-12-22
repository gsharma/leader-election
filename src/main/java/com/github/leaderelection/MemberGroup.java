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
  private final GroupEpoch epoch = new GroupEpoch();
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

}
