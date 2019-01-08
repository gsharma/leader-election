package com.github.leaderelection;

/**
 * A service listener that exists to handle requests and generate appropriate responses.
 * 
 * @author gaurav
 */
public interface ServiceHandler {

  byte[] service(final byte[] request);

}
