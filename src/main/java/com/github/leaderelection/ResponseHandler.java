package com.github.leaderelection;

/**
 * A listener that exists to field responses received from a server. Note that a process can act
 * both as a server serving requests upstream or as a client of another downstream server it needs
 * to call.
 * 
 * @author gaurav
 */
public interface ResponseHandler {
  void handleResponse(final byte[] response);
}
