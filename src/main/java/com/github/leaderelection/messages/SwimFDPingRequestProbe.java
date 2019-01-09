package com.github.leaderelection.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;
import com.github.leaderelection.RandomId;

/**
 * During each protocol period, a random member is selected from the group’s membership list and a
 * ping message sent to it. The sender then waits for a replying ack from the receiver. If this is
 * not received within the timeout (determined by the message round-trip time, which is chosen
 * smaller than the protocol period), the non-responsive member is indirectly probed via other
 * members using the FDPingRequestProbe.
 * 
 * On receipt of this FDPingRequestProbe, the receiver member simply create an FDPingProbe and send
 * the request to the member to probe. On receipt of a successful FDAckResponse, the receiver member
 * forwards this to the original senderId.
 * 
 * @author gaurav
 */
public final class SwimFDPingRequestProbe implements Request {
  private static final long serialVersionUID = 1L;

  private Id senderId;
  private Id memberToProbe;
  private Epoch epoch;
  private RequestType type = RequestType.FD_PING_REQUEST;

  public SwimFDPingRequestProbe(final Id senderId, final Epoch epoch, final Id memberToProbe) {
    this.senderId = senderId;
    this.epoch = epoch;
    this.memberToProbe = memberToProbe;
  }

  @Override
  public RequestType getType() {
    return type;
  }

  @Override
  public Id getSenderId() {
    return senderId;
  }

  @JsonDeserialize(as = RandomId.class)
  public Id getMemberToProbe() {
    return memberToProbe;
  }

  @Override
  public Epoch getEpoch() {
    return epoch;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((epoch == null) ? 0 : epoch.hashCode());
    result = prime * result + ((senderId == null) ? 0 : senderId.hashCode());
    result = prime * result + ((memberToProbe == null) ? 0 : memberToProbe.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SwimFDPingRequestProbe)) {
      return false;
    }
    SwimFDPingRequestProbe other = (SwimFDPingRequestProbe) obj;
    if (epoch == null) {
      if (other.epoch != null) {
        return false;
      }
    } else if (!epoch.equals(other.epoch)) {
      return false;
    }
    if (senderId == null) {
      if (other.senderId != null) {
        return false;
      }
    } else if (!senderId.equals(other.senderId)) {
      return false;
    }
    if (memberToProbe == null) {
      if (other.memberToProbe != null) {
        return false;
      }
    } else if (!memberToProbe.equals(other.memberToProbe)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }

  // for ser-de
  private SwimFDPingRequestProbe() {}

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("SwimFDPingRequestProbe [senderId=").append(senderId).append(", epoch=")
        .append(epoch).append(", memberToProbe=").append(memberToProbe).append(", type=")
        .append(type).append("]");
    return builder.toString();
  }

}
