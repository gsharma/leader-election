package com.github.leaderelection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.messages.OkResponse;
import com.github.leaderelection.messages.Request;
import com.github.leaderelection.messages.RequestType;
import com.github.leaderelection.messages.Response;
import com.github.leaderelection.messages.SwimFDAckResponse;
import com.github.leaderelection.messages.SwimFDPingRequestProbe;

/**
 * Service handler for various membership related service operations.
 * 
 * @author gaurav
 */
class MemberServiceHandler implements ServiceHandler {
  private static final Logger logger =
      LogManager.getLogger(MemberServiceHandler.class.getSimpleName());

  private final Member sourceMember;
  private final MemberGroup memberGroup;
  private final TCPTransport transport;

  MemberServiceHandler(final Member sourceMember, final MemberGroup memberGroup,
      final TCPTransport transport) {
    this.memberGroup = memberGroup;
    this.sourceMember = sourceMember;
    this.transport = transport;
  }

  @Override
  public byte[] service(byte[] requestPayload) {
    Request request = null;
    try {
      request = (Request) InternalLib.deserialize(requestPayload);
    } catch (Exception serdeProblem) {
      logger.error("Encountered ser-de problem", serdeProblem);
    }

    Response response = null;
    if (request != null) {
      final RequestType requestType = request.getType();
      switch (requestType) {
        case FD_PING:
          response = new SwimFDAckResponse(request.getSenderId(), request.getEpoch());
          logger.info("Received::{}, Responded with::{}", request, response);
          break;
        case FD_PING_REQUEST:
          // TODO
          final SwimFDPingRequestProbe pingRequestProbe =
              SwimFDPingRequestProbe.class.cast(request);
          final Id memberToPing = pingRequestProbe.getMemberToProbe();
          // send(memberToPing, InternalLib.serialize(pingRequestProbe));
          response = new SwimFDAckResponse(request.getSenderId(), request.getEpoch());
          logger.info("Received::{}, Responded with::{}", request, response);
          break;
        case FD_FAILED:
          break;
        case ELECTION:
          // TODO: forward to relevant members
          response = new OkResponse(request.getSenderId(), request.getEpoch());
          logger.info("Received::{}, Responded with::{}", request, response);
          break;
        case COORDINATOR:
          response = new OkResponse(request.getSenderId(), request.getEpoch());
          logger.info("Received::{}, Responded with::{}", request, response);
          break;
      }
    }

    byte[] responseBytes = null;
    if (response != null) {
      try {
        responseBytes = InternalLib.serialize(response);
      } catch (Exception serdeProblem) {
        logger.error("Encountered ser-de error", serdeProblem);
      }
    }

    return responseBytes;
  }

}
