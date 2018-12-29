package com.github.leaderelection;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for the sanity/correctness of Request, Response serialization-deserialization.
 * 
 * @author gaurav
 */
public final class SerdeTest {

  // TODO: fix me
  @Test
  public void testRequestSerDe() {
    Request request = new HeartbeatRequest(new RandomId(), new Epoch());
    byte[] serializedRequest = request.serialize();
    assertEquals(0, serializedRequest.length);
    Request deserializedRequest =
        new HeartbeatRequest(new RandomId(), new Epoch()).deserialize(serializedRequest);
    // assertEquals(request, deserializedRequest);

    request = new ElectionRequest(new RandomId(), new Epoch());
    serializedRequest = request.serialize();
    assertEquals(0, serializedRequest.length);
    deserializedRequest =
        new ElectionRequest(new RandomId(), new Epoch()).deserialize(serializedRequest);
    // assertEquals(request, deserializedRequest);

    request = new CoordinatorRequest(new RandomId(), new Epoch());
    serializedRequest = request.serialize();
    assertEquals(0, serializedRequest.length);
    deserializedRequest =
        new CoordinatorRequest(new RandomId(), new Epoch()).deserialize(serializedRequest);
    // assertEquals(request, deserializedRequest);
  }

  @Test
  public void testResponseSerDe() {}

}
