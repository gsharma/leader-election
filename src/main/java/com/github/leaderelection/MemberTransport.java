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

  public MemberTransport(final TCPTransport tcpTransport, final MemberGroup memberGroup) {
    this.tcpTransport = tcpTransport;
    this.memberGroup = memberGroup;
    // this.sourceMember = sourceMember;
  }

  public UUID bindServer(final String host, final int port) throws IOException {
    return tcpTransport.bindServer(host, port, null);
  }

  public Response dispatchTo(final Member member, final Request request) throws IOException {
    final byte[] requestBytes = request.serialize();
    final byte[] responseBytes =
        tcpTransport.send(member.getHost(), member.getPort(), requestBytes);
    return InternalLib.getObjectMapper().readValue(responseBytes, Response.class);
  }

  public void stopServer(final UUID serverId) throws IOException {
    tcpTransport.stopServer(serverId);
  }

  public void shutdown() throws IOException {
    tcpTransport.shutdown();
  }

}
