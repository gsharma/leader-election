package com.github.leaderelection;

/**
 * Listener for receiving notifications of failed members.
 * 
 * @author gaurav
 */
public interface MemberFailureListener {

  void notifyMemberFailed(Id failedMemberId) throws LeaderElectionException;

}
