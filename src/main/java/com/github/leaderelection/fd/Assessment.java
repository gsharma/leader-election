package com.github.leaderelection.fd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;
import com.github.leaderelection.MemberStatus;

/**
 * This encapsulates a very bare-bones assessment of the health of the cluster. We can do a lot
 * better than this.
 * 
 * @author gaurav
 */
public final class Assessment {
  private final Id sourceMemberId;
  private final Map<Id, MemberStatus> memberStatuses = new HashMap<>();
  private final Epoch epoch;

  public Assessment(final Id sourceMemberId, final Epoch epoch,
      final Map<Id, MemberStatus> memberStatuses) {
    this.sourceMemberId = sourceMemberId;
    this.epoch = epoch;
    if (memberStatuses != null) {
      this.memberStatuses.putAll(memberStatuses);
    }
  }

  public Id getSourceMemberId() {
    return sourceMemberId;
  }

  public Epoch getEpoch() {
    return epoch.deepCopy();
  }

  public Map<Id, MemberStatus> getMemberStatuses() {
    return Collections.unmodifiableMap(memberStatuses);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Assessment [sourceMemberId=").append(sourceMemberId).append(", memberStatuses=")
        .append(memberStatuses).append(", epoch=").append(epoch).append("]");
    return builder.toString();
  }

}
