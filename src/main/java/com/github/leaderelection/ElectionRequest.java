package com.github.leaderelection;

/**
 * Sent to announce leader election.
 * 
 * @author gaurav
 */
public final class ElectionRequest implements Request {
  private Id senderId;
  private Epoch epoch;
  private final RequestType type = RequestType.ELECTION;

  public ElectionRequest(final Id senderId, final Epoch epoch) {
    this.senderId = senderId;
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
          InternalLib.getObjectMapper().readValue(flattenedRequest, ElectionRequest.class);
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
    if (!(obj instanceof ElectionRequest)) {
      return false;
    }
    ElectionRequest other = (ElectionRequest) obj;
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

}
