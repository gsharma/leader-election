package com.github.leaderelection;

/**
 * Skeleton of leader election.
 * 
 * @author gaurav
 */
public interface LeaderElection extends MemberFailureListener {

  Member reportLeader();

  Epoch reportEpoch();

  void electLeader() throws LeaderElectionException;

  boolean isRunning();

  void shutdown();

}
