package com.github.leaderelection;

/**
 * Sent by the winner of leader election round to announce victory.
 * 
 * @author gaurav
 */
public final class CoordinatorRequest implements Request {
  private Id leaderId;
  private Epoch epoch;
  private final RequestType type = RequestType.COORDINATOR;

  public CoordinatorRequest(final Id leaderId, final Epoch epoch) {
    this.leaderId = leaderId;
    this.epoch = epoch;
  }

  @Override
  public byte[] serialize() {
    byte[] serialized = new byte[0];
    try {
      serialized = InternalLib.getObjectMapper().writeValueAsBytes(this);
    } catch (Exception serDeProblem) {
      // logger.error(String.format("Encountered error during serialization of %s", toString()),
      // serDeProblem);
    }
    return serialized;
  }

  @Override
  public Request deserialize(byte[] flattenedRequest) {
    Request deserializedRequest = null;
    try {
      deserializedRequest =
          InternalLib.getObjectMapper().readValue(flattenedRequest, CoordinatorRequest.class);
    } catch (Exception serDeProblem) {
      // logger.error("Encountered error during deserialization of flattened request",
      // serDeProblem);
    }
    return deserializedRequest;
  }

  @Override
  public RequestType getType() {
    return type;
  }

  @Override
  public Id getSenderId() {
    return leaderId;
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
    result = prime * result + ((leaderId == null) ? 0 : leaderId.hashCode());
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
    if (!(obj instanceof CoordinatorRequest)) {
      return false;
    }
    CoordinatorRequest other = (CoordinatorRequest) obj;
    if (epoch == null) {
      if (other.epoch != null) {
        return false;
      }
    } else if (!epoch.equals(other.epoch)) {
      return false;
    }
    if (leaderId == null) {
      if (other.leaderId != null) {
        return false;
      }
    } else if (!leaderId.equals(other.leaderId)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }

}
