package com.github.leaderelection;

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
