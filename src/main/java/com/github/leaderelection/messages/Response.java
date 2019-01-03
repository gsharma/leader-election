package com.github.leaderelection.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.leaderelection.Epoch;
import com.github.leaderelection.Id;
import com.github.leaderelection.InternalLib;
import com.github.leaderelection.RandomId;

/**
 * A response skeleton.
 * 
 * @author gaurav
 */
public interface Response {

  ResponseType getType();

  @JsonDeserialize(as = RandomId.class)
  Id getSenderId();

  Epoch getEpoch();

  // Deserialize and reconstruct the response object. Ideally, this should be a static
  // function not requiring the call to default response implementation constructor.
  public default Response deserialize(byte[] flattenedResponse) {
    Response deserializedResponse = null;
    try {
      deserializedResponse = InternalLib.getObjectMapper().readValue(flattenedResponse, getClass());
    } catch (Exception serDeProblem) {
      // logger.error("Encountered error during deserialization of flattened response",
      // serDeProblem);
    }
    return deserializedResponse;
  }

  // Flatten the response to a byte[]
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
