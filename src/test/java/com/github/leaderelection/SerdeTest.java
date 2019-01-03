package com.github.leaderelection;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.leaderelection.messages.CoordinatorRequest;
import com.github.leaderelection.messages.ElectionRequest;
import com.github.leaderelection.messages.HeartbeatRequest;
import com.github.leaderelection.messages.HeartbeatResponse;
import com.github.leaderelection.messages.OkResponse;
import com.github.leaderelection.messages.Request;
import com.github.leaderelection.messages.Response;

/**
 * Tests for the sanity/correctness of Request, Response serialization-deserialization.
 * 
 * @author gaurav
 */
public final class SerdeTest {

  @Test
  public void testRequestSerDe() {
    Request request = new HeartbeatRequest(new RandomId(), new Epoch());
    byte[] serializedRequest = request.serialize();
    assertEquals(97, serializedRequest.length);
    Request deserializedRequest =
        new HeartbeatRequest(new RandomId(), new Epoch()).deserialize(serializedRequest);
    assertEquals(request, deserializedRequest);

    request = new ElectionRequest(new RandomId(), new Epoch());
    serializedRequest = request.serialize();
    assertEquals(96, serializedRequest.length);
    deserializedRequest =
        new ElectionRequest(new RandomId(), new Epoch()).deserialize(serializedRequest);
    assertEquals(request, deserializedRequest);

    request = new CoordinatorRequest(new RandomId(), new Epoch());
    serializedRequest = request.serialize();
    assertEquals(99, serializedRequest.length);
    deserializedRequest =
        new CoordinatorRequest(new RandomId(), new Epoch()).deserialize(serializedRequest);
    assertEquals(request, deserializedRequest);
  }

  @Test
  public void testResponseSerDe() {
    Response response = new HeartbeatResponse(new RandomId(), new Epoch());
    byte[] serializedResponse = response.serialize();
    assertEquals(97, serializedResponse.length);
    Response deserializedResponse =
        new HeartbeatResponse(new RandomId(), new Epoch()).deserialize(serializedResponse);
    assertEquals(response, deserializedResponse);

    response = new OkResponse(new RandomId(), new Epoch());
    serializedResponse = response.serialize();
    assertEquals(90, serializedResponse.length);
    deserializedResponse =
        new OkResponse(new RandomId(), new Epoch()).deserialize(serializedResponse);
    assertEquals(response, deserializedResponse);
  }

}
