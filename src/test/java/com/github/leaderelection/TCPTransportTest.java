package com.github.leaderelection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.github.leaderelection.messages.Request;
import com.github.leaderelection.messages.SwimFDPingProbe;

/**
 * Tests for TCPTransport.
 * 
 * @author gaurav
 */
public final class TCPTransportTest {
  private static final Logger logger = LogManager.getLogger(TCPTransportTest.class.getSimpleName());

  @Test
  public void testServerLifecycle() throws Exception {
    Thread.currentThread().setName("testServerLifecycle");
    final String host = "localhost";
    int portOne = 6000;
    final ServiceHandler serviceHandler =
        null;/*
              * new ResponseHandler() {
              * 
              * @Override public void handleResponse(byte[] response) { // TODO } };
              */
    final TCPTransport transport = TCPTransport.getInstance();
    transport.start();
    try {
      final UUID serverOne = transport.bindServer(host, portOne, serviceHandler);
      assertNotNull(serverOne);
      // assertTrue(serverChannelOne.isOpen());
      // LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
      final Request payloadOne = new SwimFDPingProbe(new RandomId(), new Epoch());
      byte[] response = transport.send(serverOne, InternalLib.serialize(payloadOne));
      // assertTrue(response.length > 0);
      logger.info("Client received from server {}:{} {}bytes", host, portOne, response.length);
      transport.stopServer(serverOne);
      // assertFalse(serverChannelOne.isOpen());

      // try once more
      final int portTwo = 7000;
      final UUID serverTwo = transport.bindServer(host, portTwo, serviceHandler);
      assertNotNull(serverTwo);
      // assertTrue(serverChannelTwo.isOpen());

      // try binding to a different port
      final int portThree = 8999;
      final UUID serverThree = transport.bindServer(host, portThree, serviceHandler);
      assertNotNull(serverThree);
      // assertTrue(serverChannelThree.isOpen());

      // push payloads to both servers
      for (int iter = 0; iter < 2; iter++) {
        response = transport.send(serverTwo,
            InternalLib.serialize(new SwimFDPingProbe(new RandomId(), new Epoch())));
        // assertTrue(response.length > 0);
        logger.info("Client received from server {}:{} {}bytes", host, portTwo, response.length);

        response = transport.send(serverThree,
            InternalLib.serialize(new SwimFDPingProbe(new RandomId(), new Epoch())));
        // assertTrue(response.length > 0);
        logger.info("Client received from server {}:{} {}bytes", host, portThree, response.length);
      }

      // close both servers
      transport.stopServer(serverTwo);
      // assertFalse(serverChannelTwo.isOpen());
      transport.stopServer(serverThree);
      // assertFalse(serverChannelThree.isOpen());
    } finally {
      transport.shutdown();
      assertFalse(transport.isRunning());
    }
  }

}
