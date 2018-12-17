package com.github.leaderelection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

/**
 * Tests for TCPTransport.
 * 
 * @author gaurav
 */
public final class TCPTransportTest {

  @Test
  public void testServerLifecycle() throws IOException {
    final String host = "localhost";
    int portOne = 9000;
    final TCPTransport transport = new TCPTransport();
    final ServerSocketChannel serverChannelOne = transport.bind(host, portOne);
    assertNotNull(serverChannelOne);
    assertTrue(serverChannelOne.isOpen());
    // LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
    final String payloadOne = "one";
    transport.send(host, portOne, payloadOne);
    transport.closeServer(host, portOne);
    assertFalse(serverChannelOne.isOpen());

    // try once more, same port
    final int portTwo = portOne;
    final ServerSocketChannel serverChannelTwo = transport.bind(host, portTwo);
    assertNotNull(serverChannelTwo);
    assertTrue(serverChannelTwo.isOpen());

    // try binding to a different port
    final int portThree = 8999;
    final ServerSocketChannel serverChannelThree = transport.bind(host, portThree);
    assertNotNull(serverChannelThree);
    assertTrue(serverChannelThree.isOpen());

    // push payloads to both servers
    transport.send(host, portTwo, "two");
    transport.send(host, portThree, "three");

    // close both servers
    transport.closeServer(host, portTwo);
    assertFalse(serverChannelTwo.isOpen());
    transport.closeServer(host, portThree);
    assertFalse(serverChannelThree.isOpen());

    transport.tini();
  }

}
