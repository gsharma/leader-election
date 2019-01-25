package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

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
      logger.info("begin iteration {}", iter);
      final MemberGroup group = new MemberGroup(new RandomId());
      final String host = "localhost";

      final int portOne = port++;
      final Member memberOne = new Member(new RandomId(), host, portOne, group);
      assertNull(memberOne.getServerTransportId());
      assertTrue(memberOne.init());
      assertNotNull(memberOne.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberOne.getStatus());

      final int portTwo = port++;
      final Member memberTwo = new Member(new RandomId(), host, portTwo, group);
      assertNull(memberTwo.getServerTransportId());
      assertTrue(memberTwo.init());
      assertNotNull(memberTwo.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());

      final int portThree = port++;
      final Member memberThree = new Member(new RandomId(), host, portThree, group);
      assertNull(memberThree.getServerTransportId());
      assertTrue(memberThree.init());
      assertNotNull(memberThree.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

      for (final Member groupMember : group.allMembers()) {
        assertEquals(0L, groupMember.currentEpoch().getEpoch());
      }

      Thread.sleep(3_000L);

      LeaderElection election = null;
      try {
        final Member bully = group.greatestIdMember();
        election = new BullyLeaderElection(group, bully);
        assertTrue(election.isRunning());
        assertNull(election.reportLeader());
        election.electLeader();
        final Member leader = election.reportLeader();
        assertEquals(bully, leader);
        assertEquals(bully, group.getLeader());
        for (final Member groupMember : group.allMembers()) {
          // TODO
          // assertEquals(1L, groupMember.currentEpoch().getEpoch());
        }

        assertEquals(MemberStatus.ALIVE, memberOne.getStatus());
        assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());
        assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

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
        logger.info("finish iteration {}", iter);
      }
    }
  }

}
