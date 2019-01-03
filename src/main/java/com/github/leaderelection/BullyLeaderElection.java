package com.github.leaderelection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.leaderelection.fd.FailureDetector;

/**
 * An implementation of the bully leader election algorithm.
 * 
 * @author gaurav
 */
public final class BullyLeaderElection implements LeaderElection {
  private static final Logger logger =
      LogManager.getLogger(BullyLeaderElection.class.getSimpleName());

  private final MemberGroup memberGroup;
  private final FailureDetector failureDetector;
  private final TCPTransport transport;

  private Epoch epoch;

  public BullyLeaderElection(final MemberGroup memberGroup, final FailureDetector failureDetector,
      final TCPTransport transport) {
    this.memberGroup = memberGroup;
    this.failureDetector = failureDetector;
    this.transport = transport;
  }


  private void incrementEpoch() {
    epoch = epoch.increment();
  }

}
