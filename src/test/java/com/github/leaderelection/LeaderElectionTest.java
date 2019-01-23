package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    for (int iter = 0; iter < 1; iter++) {
      logger.info("iteration {}", iter);
      final MemberGroup group = new MemberGroup(new RandomId());
      final String host = "localhost";

      final int portOne = 4005;
      final Member memberOne = new Member(new RandomId(), host, portOne, group);
      assertNull(memberOne.getServerTransportId());
      assertTrue(memberOne.init());
      assertNotNull(memberOne.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberOne.getStatus());

      final int portTwo = 4004;
      final Member memberTwo = new Member(new RandomId(), host, portTwo, group);
      assertNull(memberTwo.getServerTransportId());
      assertTrue(memberTwo.init());
      assertNotNull(memberTwo.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberTwo.getStatus());

      final int portThree = 4003;
      final Member memberThree = new Member(new RandomId(), host, portThree, group);
      assertNull(memberThree.getServerTransportId());
      assertTrue(memberThree.init());
      assertNotNull(memberThree.getServerTransportId());
      assertEquals(MemberStatus.ALIVE, memberThree.getStatus());

      for (final Member groupMember : group.allMembers()) {
        assertEquals(0L, groupMember.currentEpoch().getEpoch());
      }

      Thread.sleep(3_000L);

      final Member bully = group.greatestIdMember();
      final LeaderElection election = new BullyLeaderElection(group, bully);
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

      election.shutdown();

      assertEquals(MemberStatus.DEAD, memberOne.getStatus());
      assertEquals(MemberStatus.DEAD, memberTwo.getStatus());
      assertEquals(MemberStatus.DEAD, memberThree.getStatus());

      assertFalse(memberOne.getTransport().isRunning());
      assertFalse(memberTwo.getTransport().isRunning());
      assertFalse(memberThree.getTransport().isRunning());
    }
  }

}
