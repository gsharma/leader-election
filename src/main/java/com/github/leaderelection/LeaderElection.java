package com.github.leaderelection;

/**
 * Skeleton of leader election.
 * 
 * @author gaurav
 */
public interface LeaderElection extends MemberFailureListener {
  Member electLeader();

  boolean shutdown();
}
