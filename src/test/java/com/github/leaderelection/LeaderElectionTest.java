package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for LeaderElection.
 * 
 * @author gaurav
 */
public class LeaderElectionTest {
  @Test
  public void testMemberGroup() throws Exception {
    final TCPTransport transport = new TCPTransport();
    try {
      final MemberGroup group = new MemberGroup(new RandomId());
      final String host = "localhost";

      final int portOne = 4005;
      final Member memberOne = new Member(transport, new RandomId(), host, portOne, group);
      assertNull(memberOne.getServerTransportId());
      memberOne.init();
      assertNotNull(memberOne.getServerTransportId());
      assertEquals(Status.ALIVE, memberOne.getStatus());

      final int portTwo = 4004;
      final Member memberTwo = new Member(transport, new RandomId(), host, portTwo, group);
      assertNull(memberTwo.getServerTransportId());
      memberTwo.init();
      assertNotNull(memberTwo.getServerTransportId());
      assertEquals(Status.ALIVE, memberTwo.getStatus());

      final int portThree = 4003;
      final Member memberThree = new Member(transport, new RandomId(), host, portThree, group);
      assertNull(memberThree.getServerTransportId());
      memberThree.init();
      assertNotNull(memberThree.getServerTransportId());
      assertEquals(Status.ALIVE, memberThree.getStatus());

      Thread.sleep(3_000L);

      final Member bully = group.greatestIdMember();
      final LeaderElection election = new BullyLeaderElection(group, bully, transport);
      final Member leader = election.electLeader();
      assertEquals(bully, leader);

      memberOne.shutdown();
      assertEquals(Status.DEAD, memberOne.getStatus());
      memberTwo.shutdown();
      assertEquals(Status.DEAD, memberTwo.getStatus());
      memberThree.shutdown();
      assertEquals(Status.DEAD, memberThree.getStatus());
    } finally {
      transport.shutdown();
    }
  }

}
