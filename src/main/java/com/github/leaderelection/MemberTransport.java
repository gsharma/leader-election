package com.github.leaderelection;

import java.io.IOException;
import java.util.UUID;

import com.github.leaderelection.messages.Request;
import com.github.leaderelection.messages.Response;

/**
 * This class serves as the transport proxy for a member and abstracts away any transport-specific
 * data and logic in the underlying tcp transport layer.
 * 
 * @author gaurav
 */
public final class MemberTransport {
  private final MemberGroup memberGroup;
  private final TCPTransport tcpTransport;

  public MemberTransport(final MemberGroup memberGroup) {
    this.tcpTransport = TCPTransport.getInstance();
    this.tcpTransport.start();
    this.memberGroup = memberGroup;
    // this.sourceMember = sourceMember;
  }

  public UUID bindServer(final String host, final int port) throws IOException {
    return tcpTransport.bindServer(host, port, null);
  }

  public Response dispatchTo(final Member member, final Request request) throws IOException {
    Response response = null;
    if (isRunning()) {
      final byte[] requestBytes = InternalLib.serialize(request);
      // final byte[] requestBytes = request.serialize();
      final byte[] responseBytes =
          tcpTransport.send(member.getHost(), member.getPort(), requestBytes);
      if (responseBytes != null && responseBytes.length != 0) {
        response = Response.class.cast(InternalLib.deserialize(responseBytes));
        // return InternalLib.getObjectMapper().readValue(responseBytes, Response.class);
      }
    }
    return response;
  }

  public void stopServer(final UUID serverId) throws IOException {
    tcpTransport.stopServer(serverId);
  }

  public void shutdown() throws IOException {
    tcpTransport.shutdown();
  }

  public boolean isRunning() {
    return tcpTransport.isRunning();
  }

}
