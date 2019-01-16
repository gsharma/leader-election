package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for LeaderElection.
 * 
 * @author gaurav
 */
public class LeaderElectionTest {

  @Test
  public void testBullyLeaderElection() throws Exception {
    final MemberGroup group = new MemberGroup(new RandomId());
    final String host = "localhost";

    final int portOne = 4005;
    final Member memberOne = new Member(new RandomId(), host, portOne, group);
    assertNull(memberOne.getServerTransportId());
    assertTrue(memberOne.init());
    assertNotNull(memberOne.getServerTransportId());
    assertEquals(Status.ALIVE, memberOne.getStatus());

    final int portTwo = 4004;
    final Member memberTwo = new Member(new RandomId(), host, portTwo, group);
    assertNull(memberTwo.getServerTransportId());
    assertTrue(memberTwo.init());
    assertNotNull(memberTwo.getServerTransportId());
    assertEquals(Status.ALIVE, memberTwo.getStatus());

    final int portThree = 4003;
    final Member memberThree = new Member(new RandomId(), host, portThree, group);
    assertNull(memberThree.getServerTransportId());
    assertTrue(memberThree.init());
    assertNotNull(memberThree.getServerTransportId());
    assertEquals(Status.ALIVE, memberThree.getStatus());

    for (final Member groupMember : group.allMembers()) {
      assertEquals(0L, groupMember.currentEpoch().getEpoch());
    }

    Thread.sleep(3_000L);

    final Member bully = group.greatestIdMember();
    final LeaderElection election = new BullyLeaderElection(group, bully);
    final Member leader = election.electLeader();
    assertEquals(bully, leader);
    assertEquals(bully, group.getLeader());
    for (final Member groupMember : group.allMembers()) {
      // TODO
      // assertEquals(1L, groupMember.currentEpoch().getEpoch());
    }

    election.shutdown();

    assertEquals(Status.DEAD, memberOne.getStatus());
    assertEquals(Status.DEAD, memberTwo.getStatus());
    assertEquals(Status.DEAD, memberThree.getStatus());

    assertFalse(memberOne.getTransport().isRunning());
    assertFalse(memberTwo.getTransport().isRunning());
    assertFalse(memberThree.getTransport().isRunning());
  }

}
