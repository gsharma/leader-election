package com.github.leaderelection;

/**
 * Skeleton for a failure detector.
 * 
 * @author gaurav
 */
public interface FailureDetector {

  boolean init();

  boolean tini();

  boolean addMember(Member member);

  boolean removeMember(Member member);

  Assessment assess();

}
