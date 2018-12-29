package com.github.leaderelection;

/**
 * Request skeleton.
 * 
 * @author gaurav
 */
public interface Request {
  // Flatten the request to a byte[]
  byte[] serialize();

  // Deserialize and reconstruct the request object. Ideally, this should be a static
  // function not requiring the call to default request implementation constructor.
  Request deserialize(byte[] flattenedRequest);

  RequestType getType();

  Id getSenderId();

  Epoch getEpoch();

}
