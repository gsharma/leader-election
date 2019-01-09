package com.github.leaderelection.messages;

import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;

/**
 * A Ping Probe sent by the Failure Detector (FD) to an arbitrary member in the group.
 * 
 * During each protocol period, a random member is selected from the group’s membership list and a
 * ping message sent to it. The sender then waits for a replying ack from the receiver. If this is
 * not received within the timeout (determined by the message round-trip time, which is chosen
 * smaller than the protocol period), the non-responsive member is indirectly probed via other
 * members using the FDPingRequestProbe.
 * 
 * @author gaurav
 */
public final class SwimFDPingProbe implements Request {
  private static final long serialVersionUID = 1L;

  private Id senderId;
  private Epoch epoch;
  private RequestType type = RequestType.FD_PING;

  public SwimFDPingProbe(final Id senderId, final Epoch epoch) {
    this.senderId = senderId;
    this.epoch = epoch;
  }

  @Override
  public RequestType getType() {
    return type;
  }

  @Override
  public Id getSenderId() {
    return senderId;
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
    if (!(obj instanceof SwimFDPingProbe)) {
      return false;
    }
    SwimFDPingProbe other = (SwimFDPingProbe) obj;
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
    if (type != other.type) {
      return false;
    }
    return true;
  }

  // for ser-de
  private SwimFDPingProbe() {}

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("SwimFDPingProbe [senderId=").append(senderId).append(", epoch=").append(epoch)
        .append(", type=").append(type).append("]");
    return builder.toString();
  }

}
