package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

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

        Thread.sleep(3_000L);

        Member bully = group.greatestIdMember();
        election = new BullyLeaderElection(group, bully);

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

          assertEquals(MemberStatus.ALIVE, memberOne.getStatus());
          assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());
          assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

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
        Thread.sleep(500L);
        logger.info("Finish overall iter {}", iter);
      }
    }
  }

  @Test
  public void testMembershipChange() throws Exception {
    int port = 5003;
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

      Thread.sleep(3_000L);

      Member bully = group.greatestIdMember();
      election = new BullyLeaderElection(group, bully);

      logger.info("Begin election");
      assertTrue(election.isRunning());
      assertNull(election.reportLeader());
      assertEquals(expectedEpoch, election.reportEpoch().getEpoch());
      election.electLeader();
      final Member leader = election.reportLeader();
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
      for (Map.Entry<Id, MemberStatus> statusEntry : bully.getFailureAssessment()
          .getMemberStatuses().entrySet()) {
        assertEquals(MemberStatus.ALIVE, statusEntry.getValue());
      }
      logger.info("Finish election");

      // TODO: remove a member from group
      assertTrue(group.removeMember(memberOne));
      assertEquals(MemberStatus.DEAD, memberOne.getStatus());
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
      Thread.sleep(500L);
    }
  }

}
