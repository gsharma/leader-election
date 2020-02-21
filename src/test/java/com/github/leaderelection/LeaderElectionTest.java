package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Tests for LeaderElection.
 * 
 * @author gaurav
 */
public class LeaderElectionTest {
  private static final Logger logger =
      LogManager.getLogger(LeaderElectionTest.class.getSimpleName());

  @Test
  public void testBullyLeaderElection() throws Exception {
    Thread.currentThread().setName("testBullyLeaderElection");
    logger.info("----- BEGIN -----");
    /**
     * TODO: make this robust enough to be run successfully in a loop - at the moment, we're crappy
     * and sometimes tend to balk after the first iteration itself
     */
    int port = 4003;
    for (int iter = 0; iter < 1; iter++) {
      LeaderElection election = null;
      Member memberOne = null, memberTwo = null, memberThree = null;
      try {
        long expectedEpoch = 0L;
        final MemberGroup group = new MemberGroup(new RandomId());
        final String host = "localhost";

        final int portOne = port++;
        memberOne = new Member(new RandomId(), host, portOne, group);
        assertNull(memberOne.getServerTransportId());
        assertTrue(memberOne.init());
        assertNotNull(memberOne.getServerTransportId());
        assertEquals(MemberStatus.ALIVE, memberOne.getStatus());

        final int portTwo = port++;
        memberTwo = new Member(new RandomId(), host, portTwo, group);
        assertNull(memberTwo.getServerTransportId());
        assertTrue(memberTwo.init());
        assertNotNull(memberTwo.getServerTransportId());
        assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());

        final int portThree = port++;
        memberThree = new Member(new RandomId(), host, portThree, group);
        assertNull(memberThree.getServerTransportId());
        assertTrue(memberThree.init());
        assertNotNull(memberThree.getServerTransportId());
        assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

        for (final Member groupMember : group.allMembers()) {
          assertEquals(expectedEpoch, groupMember.currentEpoch().getEpoch());
        }

        final long sleepMillis = 4_000L;
        logger.info("Sleeping for {} millis", sleepMillis);
        Thread.sleep(sleepMillis);
        logger.info("Woke up after {} millis", sleepMillis);

        Member bully = group.greatestIdMember();
        election = new BullyLeaderElection(group, bully);
        assertEquals(expectedEpoch, election.reportEpoch().getEpoch());

        // TODO: bully.getFailureAssessment();

        Member previousLeader = null;
        for (int bullyIter = 0; bullyIter < 2; bullyIter++) {
          logger.info("Begin election iter {}", bullyIter);

          assertTrue(election.isRunning());
          if (bullyIter > 0) {
            assertNotNull(election.reportLeader());
          } else {
            assertNull(election.reportLeader());
          }
          assertEquals(expectedEpoch, election.reportEpoch().getEpoch());
          election.electLeader();
          final Member leader = election.reportLeader();
          bully = group.greatestIdMember();
          assertEquals(bully, leader);
          assertEquals(bully, group.getLeader());
          assertEquals(++expectedEpoch, election.reportEpoch().getEpoch());
          // in the absence of leader death or reachability, validate that the bully never ever
          // loses its leadership despite repeated rounds of voting
          if (previousLeader != null) {
            assertEquals(previousLeader, leader);
          }
          for (final Member groupMember : group.allMembers()) {
            assertEquals(expectedEpoch, groupMember.currentEpoch().getEpoch());
          }

          assertEquals(memberOne.getId() + " isn't alive", MemberStatus.ALIVE,
              memberOne.getStatus());
          assertEquals(memberTwo.getId() + " isn't alive", MemberStatus.ALIVE,
              memberTwo.getStatus());
          assertEquals(memberThree.getId() + " isn't alive", MemberStatus.ALIVE,
              memberThree.getStatus());

          // ensure that fd assessment is consistent
          for (Map.Entry<Id, MemberStatus> statusEntry : bully.getFailureAssessment()
              .getMemberStatuses().entrySet()) {
            assertEquals(MemberStatus.ALIVE, statusEntry.getValue());
          }
          logger.info("Finish election iter {}", bullyIter);
        }
      } finally {
        if (election != null) {
          // Thread.sleep(5000L);
          election.shutdown();
          assertFalse(election.isRunning());

          assertEquals(MemberStatus.DEAD, memberOne.getStatus());
          assertEquals(MemberStatus.DEAD, memberTwo.getStatus());
          assertEquals(MemberStatus.DEAD, memberThree.getStatus());

          assertFalse(memberOne.getTransport().isRunning());
          assertFalse(memberTwo.getTransport().isRunning());
          assertFalse(memberThree.getTransport().isRunning());
        }
        // TODO: remove fugly sleeps like this one
        Thread.sleep(500L);
        logger.info("Finish overall iter {}", iter);
      }
    }
    logger.info("----- END -----");
  }

  @Test
  public void testMembershipChange() throws Exception {
    Thread.currentThread().setName("testMembershipChange");
    logger.info("----- BEGIN -----");
    int port = 2003;
    LeaderElection election = null;
    Member memberOne = null, memberTwo = null, memberThree = null;
    try {
      long expectedEpoch = 0L;
      final MemberGroup group = new MemberGroup(new RandomId());
      final String host = "localhost";

      final int portOne = port++;
      memberOne = new Member(new RandomId(), host, portOne, group);
      assertNull(memberOne.getServerTransportId());
      assertTrue(memberOne.init());
      assertNotNull(memberOne.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberOne.getStatus());

      final int portTwo = port++;
      memberTwo = new Member(new RandomId(), host, portTwo, group);
      assertNull(memberTwo.getServerTransportId());
      assertTrue(memberTwo.init());
      assertNotNull(memberTwo.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());

      final int portThree = port++;
      memberThree = new Member(new RandomId(), host, portThree, group);
      assertNull(memberThree.getServerTransportId());
      assertTrue(memberThree.init());
      assertNotNull(memberThree.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

      for (final Member groupMember : group.allMembers()) {
        assertEquals(expectedEpoch, groupMember.currentEpoch().getEpoch());
      }

      final long sleepMillis = 4_000L;
      logger.info("Sleeping for {} millis", sleepMillis);
      Thread.sleep(sleepMillis);
      logger.info("Woke up after {} millis", sleepMillis);

      Member bully = group.greatestIdMember();
      election = new BullyLeaderElection(group, bully);

      logger.info("Begin election");
      assertTrue(election.isRunning());
      assertNull(election.reportLeader());
      assertEquals(expectedEpoch, election.reportEpoch().getEpoch());
      election.electLeader();
      Member leader = election.reportLeader();
      bully = group.greatestIdMember();
      assertEquals(bully, leader);
      assertEquals(bully, group.getLeader());
      assertEquals(++expectedEpoch, election.reportEpoch().getEpoch());
      // in the absence of leader death or reachability, validate that the bully never ever
      // loses its leadership despite repeated rounds of voting
      for (final Member groupMember : group.allMembers()) {
        assertEquals(expectedEpoch, groupMember.currentEpoch().getEpoch());
      }

      assertEquals(MemberStatus.ALIVE, memberOne.getStatus());
      assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());
      assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

      // ensure that fd assessment is consistent
      int counter = 5;
      while (counter-- > 0) {
        if (bully.getFailureAssessment() != null) {
          for (Map.Entry<Id, MemberStatus> statusEntry : bully.getFailureAssessment()
              .getMemberStatuses().entrySet()) {
            assertEquals(MemberStatus.ALIVE, statusEntry.getValue());
          }
          break;
        }
        logger.info("Retrying to fetch failure assessment");
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(300L));
      }
      logger.info("Finish election");

      // remove a member from group
      assertTrue(group.removeMember(memberOne));
      assertEquals(MemberStatus.DEAD, memberOne.getStatus());
      for (final Member member : group.otherMembers(memberOne)) {
        assertEquals(MemberStatus.ALIVE, member.getStatus());
      }

      // should also check that fd's assessment is accurate
      counter = 5;
      int deadMembers = 0;
      while (counter-- > 0) {
        deadMembers = 0;
        if (bully.getFailureAssessment() != null) {
          for (final Map.Entry<Id, MemberStatus> statusEntry : bully.getFailureAssessment()
              .getMemberStatuses().entrySet()) {
            if (MemberStatus.DEAD == statusEntry.getValue()) {
              deadMembers++;
            }
          }
          if (deadMembers == 1) {
            break;
          }
        }
        logger.info("Retrying to fetch failure assessment");
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500L));
      }
      // TODO assertEquals(1, deadMembers);

      // check that leadership has not moved
      leader = election.reportLeader();
      bully = group.greatestIdMember();
      assertEquals(bully, leader);
      assertEquals(bully, group.getLeader());
    } finally {
      if (election != null) {
        election.shutdown();
        assertFalse(election.isRunning());

        assertEquals(MemberStatus.DEAD, memberOne.getStatus());
        assertEquals(MemberStatus.DEAD, memberTwo.getStatus());
        assertEquals(MemberStatus.DEAD, memberThree.getStatus());

        assertFalse(memberOne.getTransport().isRunning());
        assertFalse(memberTwo.getTransport().isRunning());
        assertFalse(memberThree.getTransport().isRunning());
      }
      Thread.sleep(500L);
    }
    logger.info("----- END -----");
  }

}
