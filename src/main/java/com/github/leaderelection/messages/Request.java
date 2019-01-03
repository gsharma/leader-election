package com.github.leaderelection.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;
import com.github.leaderelection.InternalLib;
import com.github.leaderelection.RandomId;

/**
 * A request skeleton.
 * 
 * @author gaurav
 */
public interface Request {

  RequestType getType();

  @JsonDeserialize(as = RandomId.class)
  Id getSenderId();

  Epoch getEpoch();

  // Deserialize and reconstruct the request object. Ideally, this should be a static
  // function not requiring the call to default request implementation constructor.
  public default Request deserialize(byte[] flattenedRequest) {
    Request deserializedRequest = null;
    try {
      deserializedRequest = InternalLib.getObjectMapper().readValue(flattenedRequest, getClass());
    } catch (Exception serDeProblem) {
      // logger.error("Encountered error during deserialization of flattened request",
      // serDeProblem);
    }
    return deserializedRequest;
  }

  // Flatten the request to a byte[]
  public default byte[] serialize() {
    byte[] serialized = new byte[0];
    try {
      serialized = InternalLib.getObjectMapper().writeValueAsBytes(this);
    } catch (Exception serDeProblem) {
      // logger.error(String.format("Encountered error during serialization of %s", toString()),
      // serDeProblem);
    }
    return serialized;
  }

}
