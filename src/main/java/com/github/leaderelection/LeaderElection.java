package com.github.leaderelection;

/**
 * Skeleton of leader election.
 * 
 * @author gaurav
 */
public interface LeaderElection {
  Member electLeader();

  boolean shutdown();
}
