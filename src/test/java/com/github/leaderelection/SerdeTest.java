package com.github.leaderelection;

import static org.junit.Assert.*;

import java.io.Serializable;

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

  /*@Test
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
  }*/

  @Test
  public void testSerDe() {
    SerdeMock mock = new SerdeMock();
    mock.f = 9.0f;
    mock.i = 8;
    mock.s = "s";
    byte[] flattened = InternalLib.serialize(mock);
    Object deserialized = InternalLib.deserialize(flattened);
    assertEquals(mock, deserialized);
  }

  public static class SerdeMock implements Serializable {
    private static final long serialVersionUID = 1L;
    private String s;
    private Integer i;
    private float f;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Float.floatToIntBits(f);
      result = prime * result + ((i == null) ? 0 : i.hashCode());
      result = prime * result + ((s == null) ? 0 : s.hashCode());
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
      if (!(obj instanceof SerdeMock)) {
        return false;
      }
      SerdeMock other = (SerdeMock) obj;
      if (Float.floatToIntBits(f) != Float.floatToIntBits(other.f)) {
        return false;
      }
      if (i == null) {
        if (other.i != null) {
          return false;
        }
      } else if (!i.equals(other.i)) {
        return false;
      }
      if (s == null) {
        if (other.s != null) {
          return false;
        }
      } else if (!s.equals(other.s)) {
        return false;
      }
      return true;
    }
  }
}
