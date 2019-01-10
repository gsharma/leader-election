package com.github.leaderelection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for sanctity of MemberGroup.
 * 
 * @author gaurav
 */
public class MemberGroupTest {

  @Test
  public void testMemberGroup() throws Exception {
    final MemberGroup group = new MemberGroup(new RandomId());
    // final MemberTransport transport = new MemberTransport(group);

    final String host = "localhost";

    final int portOne = 4005;
    final Member memberOne = new Member(new RandomId(), host, portOne, group);
    assertNull(memberOne.getServerTransportId());
    memberOne.init();
    assertNotNull(memberOne.getServerTransportId());
    assertEquals(Status.ALIVE, memberOne.getStatus());

    final int portTwo = 4004;
    final Member memberTwo = new Member(new RandomId(), host, portTwo, group);
    assertNull(memberTwo.getServerTransportId());
    memberTwo.init();
    assertNotNull(memberTwo.getServerTransportId());
    assertEquals(Status.ALIVE, memberTwo.getStatus());

    final int portThree = 4003;
    final Member memberThree = new Member(new RandomId(), host, portThree, group);
    assertNull(memberThree.getServerTransportId());
    memberThree.init();
    assertNotNull(memberThree.getServerTransportId());
    assertEquals(Status.ALIVE, memberThree.getStatus());

    Thread.sleep(3_000L);

    memberOne.shutdown();
    assertEquals(Status.DEAD, memberOne.getStatus());
    memberTwo.shutdown();
    assertEquals(Status.DEAD, memberTwo.getStatus());
    memberThree.shutdown();
    assertEquals(Status.DEAD, memberThree.getStatus());

    assertFalse(memberOne.getTransport().isRunning());
    assertFalse(memberTwo.getTransport().isRunning());
    assertFalse(memberThree.getTransport().isRunning());

    // } finally {
    // transport.shutdown();
    // assertFalse(transport.isRunning());
    // }
  }

}
