package com.github.leaderelection;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Tests for TCPTransport.
 * 
 * @author gaurav
 */
public final class TCPTransportTest {
  private static final Logger logger = LogManager.getLogger(TCPTransport.class.getSimpleName());

  @Test
  public void testServerLifecycle() throws IOException {
    final String host = "localhost";
    int portOne = 5000;
    final ResponseHandler responseHandler = new ResponseHandler() {
      @Override
      public void handleResponse(byte[] response) {
        // TODO
      }
    };
    final TCPTransport transport = new TCPTransport();
    final UUID serverOne = transport.bindServer(host, portOne, responseHandler);
    assertNotNull(serverOne);
    // assertTrue(serverChannelOne.isOpen());
    // LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
    final String payloadOne = "one";
    byte[] response = transport.send(serverOne, payloadOne.getBytes());
    assertNotNull(response);
    logger.info("Client received from server {}:{} response:{}, {} bytes", host, portOne,
        new String(response), response.length);
    transport.stopServer(serverOne);
    // assertFalse(serverChannelOne.isOpen());

    // try once more, same port
    final int portTwo = portOne;
    final UUID serverTwo = transport.bindServer(host, portTwo, responseHandler);
    assertNotNull(serverTwo);
    // assertTrue(serverChannelTwo.isOpen());

    // try binding to a different port
    final int portThree = 8999;
    final UUID serverThree = transport.bindServer(host, portThree, responseHandler);
    assertNotNull(serverThree);
    // assertTrue(serverChannelThree.isOpen());

    // push payloads to both servers
    for (int iter = 0; iter < 2; iter++) {
      response = transport.send(serverTwo, "two".getBytes());
      assertNotNull(response);
      logger.info("Client received from server {}:{} response:{}, {} bytes", host, portTwo,
          new String(response), response.length);

      response = transport.send(serverThree, "three".getBytes());
      assertNotNull(response);
      logger.info("Client received from server {}:{} response:{}, {} bytes", host, portThree,
          new String(response), response.length);
    }

    // close both servers
    transport.stopServer(serverTwo);
    // assertFalse(serverChannelTwo.isOpen());
    transport.stopServer(serverThree);
    // assertFalse(serverChannelThree.isOpen());

    transport.shutdown();
  }

}
