package com.github.leaderelection;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.messages.Request;
import com.github.leaderelection.messages.Response;

/**
 * This class serves as the transport proxy for a member and abstracts away any transport-specific
 * data and logic in the underlying tcp transport layer.
 * 
 * @author gaurav
 */
public final class MemberTransport {
  private static final Logger logger = LogManager.getLogger(MemberTransport.class.getSimpleName());

  private final Member sourceMember;
  private final MemberGroup memberGroup;
  private final TCPTransport tcpTransport;
  private final ServiceHandler serviceHandler;

  public MemberTransport(final Member sourceMember, final MemberGroup memberGroup) {
    this.tcpTransport = TCPTransport.getInstance();
    this.tcpTransport.start();
    this.memberGroup = memberGroup;
    this.sourceMember = sourceMember;
    this.serviceHandler =
        new MemberServiceHandler(this.sourceMember, this.memberGroup, this.tcpTransport);
  }

  public UUID bindServer(final String host, final int port) throws Exception {
    return tcpTransport.bindServer(host, port, serviceHandler);
  }

  public Response dispatchTo(final Member destinationMember, final Request request)
      throws Exception {
    Response response = null;
    if (isRunning()) {
      final byte[] requestBytes = InternalLib.serialize(request);
      // final byte[] requestBytes = request.serialize();
      final byte[] responseBytes =
          tcpTransport.send(destinationMember.getHost(), destinationMember.getPort(), requestBytes);
      if (responseBytes != null && responseBytes.length != 0) {
        response = Response.class.cast(InternalLib.deserialize(responseBytes));
        // return InternalLib.getObjectMapper().readValue(responseBytes, Response.class);
      }
      logger.info("Dispatched {}, received {}", request, response);
    } else {
      logger.warn("Cannot dispatch {} to {}:{} with a stopped transport layer", request,
          destinationMember.getHost(), destinationMember.getPort());
    }
    return response;
  }

  public void stopServer(final UUID serverId) throws Exception {
    tcpTransport.stopServer(serverId);
  }

  public void shutdown() throws Exception {
    tcpTransport.shutdown();
  }

  public boolean isRunning() {
    return tcpTransport.isRunning();
  }

}
