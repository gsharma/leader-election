package com.github.leaderelection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple TCP transport handler.
 * 
 * @author gaurav
 */
public final class TCPTransport {
  private static final Logger logger = LogManager.getLogger(TCPTransport.class.getSimpleName());

  private final ConcurrentMap<String, ServerSocketChannel> liveChannels = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Thread> activeListeners = new ConcurrentHashMap<>();

  /**
   * Bind and create a server socket listening on the given host,port.
   */
  public ServerSocketChannel bind(final String host, final int port) throws IOException {
    logger.info("Preparing server to listen on {}:{}", host, port);
    ServerSocketChannel serverChannel = liveChannels.get(key(host, port));
    if (serverChannel != null && serverChannel.isOpen()) {
      return serverChannel;
    }
    serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);
    serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
    serverChannel.bind(new InetSocketAddress(host, port));
    liveChannels.putIfAbsent(key(host, port), serverChannel);
    final ServerListener serverListener = new ServerListener(serverChannel);
    serverListener.start();
    activeListeners.putIfAbsent(key(host, port), serverListener);
    return serverChannel;
  }

  /**
   * TODO: register a listener
   */
  private void accept(final ServerSocketChannel serverChannel) throws IOException {
    // TODO: cleanup the dupe receptions
    logger.info("Server ready to accept requests on {}", serverChannel.getLocalAddress());
    final Selector selector = Selector.open();
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    final ByteBuffer buffer = ByteBuffer.allocate(256);
    while (true) {
      final int keys = selector.select();
      final Set<SelectionKey> selectedKeys = selector.selectedKeys();
      final Iterator<SelectionKey> iterator = selectedKeys.iterator();
      while (iterator.hasNext()) {
        final SelectionKey key = iterator.next();
        if (key.isAcceptable()) {
          final SocketChannel clientChannel = serverChannel.accept();
          clientChannel.configureBlocking(false);
          clientChannel.register(selector, SelectionKey.OP_READ);
        }
        if (key.isReadable()) {
          final SocketChannel clientChannel = (SocketChannel) key.channel();
          clientChannel.read(buffer);
          buffer.flip();
          logger.info("Server received from client {} payload:{}", clientChannel.getRemoteAddress(),
              new String(buffer.array()).trim());
          // tmp: echoing back to client
          clientChannel.write(buffer);
          buffer.clear();
        }
        iterator.remove();
      }
    }
  }

  // TODO: finish me
  public void send(final String host, final int port, final String payload) throws IOException {
    logger.info("Client sending to server {}:{} payload:{}", host, port, payload);
    final SocketChannel clientChannel = SocketChannel.open(new InetSocketAddress(host, port));
    // clientChannel.configureBlocking(false);
    final ByteBuffer buffer = ByteBuffer.wrap(payload.getBytes());
    clientChannel.write(buffer);
    buffer.clear();
    clientChannel.read(buffer);
    final String serverResponse = new String(buffer.array()).trim();
    logger.info("Client received from server {}:{} response:{}", host, port, serverResponse);
    buffer.clear();
    clientChannel.close();
  }

  /**
   * Close the server socket listening on the given host and port.
   */
  public void closeServer(final String host, final int port) throws IOException {
    final String key = key(host, port);
    ServerSocketChannel serverChannel = liveChannels.get(key);
    close(serverChannel);
    liveChannels.remove(key(host, port));
    final Thread listenerThread = activeListeners.get(key);
    listenerThread.interrupt();
    activeListeners.remove(key);
  }

  public void tini() throws IOException {
    for (final Map.Entry<String, ServerSocketChannel> liveChannelEntry : liveChannels.entrySet()) {
      final String address = liveChannelEntry.getKey();
      final ServerSocketChannel serverChannel = liveChannelEntry.getValue();
      close(serverChannel);
    }
  }

  /**
   * Close the given server socket.
   */
  private void close(final ServerSocketChannel serverChannel) throws IOException {
    if (serverChannel != null && serverChannel.isOpen()) {
      logger.info("Closing server socket listening on {}", serverChannel.getLocalAddress());
      serverChannel.close();
    }
  }

  private static String key(final String host, final int port) {
    return host + ':' + port;
  }

  private final class ServerListener extends Thread {
    private final ServerSocketChannel serverChannel;

    private ServerListener(final ServerSocketChannel serverChannel) {
      setDaemon(true);
      this.serverChannel = serverChannel;
      try {
        final InetSocketAddress address = (InetSocketAddress) serverChannel.getLocalAddress();
        setName("acceptor-" + address.getPort());
      } catch (IOException problem) {
      }
    }

    @Override
    public void run() {
      try {
        logger.info("Started acceptor on {}", serverChannel.getLocalAddress());
        while (!interrupted()) {
          accept(serverChannel);
          LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100L));
        }
        logger.info("Closing acceptor on {}", serverChannel.getLocalAddress());
      } catch (IOException problem) {
      }
    }
  }

}
