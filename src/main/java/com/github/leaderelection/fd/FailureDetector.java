package com.github.leaderelection.fd;

import com.github.leaderelection.MemberGroup;

/**
 * Skeleton for a failure detector.
 * 
 * @author gaurav
 */
public interface FailureDetector {

  boolean init();

  boolean tini();

  Assessment assess();

  MemberGroup members();

}
