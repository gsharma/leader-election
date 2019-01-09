package com.github.leaderelection.messages;

import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;

/**
 * The FDAckResponse is received in response to either the FDPingProbe or to any subsequent
 * FDPingRequestProbe messages. The FDAckResponse received by the mediatorId member is further sent
 * by the mediatorId member to the original FDPingRequestProbe's sender.
 * 
 * @author gaurav
 */
public final class SwimFDAckResponse implements Response {
  private static final long serialVersionUID = 1L;

  private Id senderId;
  // The mediatorId is optionally set only as an ack response to the delegated FDPingRequestProbe
  // message
  private Id mediatorId;
  private Epoch epoch;
  private ResponseType type = ResponseType.ACK;

  public SwimFDAckResponse(final Id senderId, final Epoch epoch) {
    this.senderId = senderId;
    this.epoch = epoch;
  }

  @Override
  public ResponseType getType() {
    return type;
  }

  @Override
  public Id getSenderId() {
    return senderId;
  }

  public Id getMediatorId() {
    return mediatorId;
  }

  @Override
  public Epoch getEpoch() {
    return epoch;
  }

  // for ser-de
  private SwimFDAckResponse() {}

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
    if (!(obj instanceof SwimFDAckResponse)) {
      return false;
    }
    SwimFDAckResponse other = (SwimFDAckResponse) obj;
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("SwimFDAckResponse [senderId=").append(senderId).append(", mediatorId=")
        .append(mediatorId).append(", epoch=").append(epoch).append(", type=").append(type)
        .append("]");
    return builder.toString();
  }

}
