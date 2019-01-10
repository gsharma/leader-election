package com.github.leaderelection;

/**
 * TODO
 * 
 * @author gaurav
 */
class ServiceHandler {
  private final Member sourceMember;
  private final MemberGroup memberGroup;
  private final TCPTransport transport;

  ServiceHandler(final Member sourceMember, final MemberGroup memberGroup,
      final TCPTransport transport) {
    this.memberGroup = memberGroup;
    this.sourceMember = sourceMember;
    this.transport = transport;
  }

  // TODO
  byte[] service(byte[] request) {
    return null;
  }

}
