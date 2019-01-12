package com.github.leaderelection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.messages.OkResponse;
import com.github.leaderelection.messages.Request;
import com.github.leaderelection.messages.RequestType;
import com.github.leaderelection.messages.Response;
import com.github.leaderelection.messages.SwimFDAckResponse;
import com.github.leaderelection.messages.SwimFDPingProbe;
import com.github.leaderelection.messages.SwimFDPingRequestProbe;

/**
 * Service handler for various membership related service operations. Note that this provides a
 * callback handler that is asynchronously invoked by the transport layer to help service Member
 * requests.
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
  public byte[] service(byte[] flattenedRequest) {
    Request request = null;
    try {
      request = (Request) InternalLib.deserialize(flattenedRequest);
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
          final SwimFDPingRequestProbe pingRequestProbe =
              SwimFDPingRequestProbe.class.cast(request);
          final Id memberToPing = pingRequestProbe.getMemberToProbe();
          final Member memberToProbe = memberGroup.findMember(memberToPing);
          final SwimFDPingProbe pingProbe =
              new SwimFDPingProbe(sourceMember.getId(), sourceMember.currentEpoch());
          final byte[] requestPayload = InternalLib.serialize(pingProbe);
          SwimFDAckResponse pingRequestResponse = null;
          try {
            final byte[] responsePayload =
                transport.send(memberToProbe.getHost(), memberToProbe.getPort(), requestPayload);
            if (responsePayload != null) {
              pingRequestResponse =
                  SwimFDAckResponse.class.cast(InternalLib.deserialize(responsePayload));
            }
          } catch (Exception problem) {
            logger.error("Failed in ping-request probe to " + memberToProbe, problem);
          }
          if (pingRequestResponse != null) {
            response = new SwimFDAckResponse(sourceMember.getId(), sourceMember.currentEpoch());
          } else {
            // hmm what should we do - possibly nack?
          }
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
    } else {
      responseBytes = new byte[0];
    }

    return responseBytes;
  }

}
