package com.github.leaderelection;

/**
 * Skeleton for a member server participating in leader election as part of a group of servers.
 * 
 * @author gaurav
 */
public final class Member {
  private final Id id;
  private final String host;
  private final int port;

  public Member(final Id id, final String host, final int port) {
    this.id = id;
    this.host = host;
    this.port = port;
  }

  public Id getId() {
    return id;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + port;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Member)) {
      return false;
    }
    Member other = (Member) obj;
    if (host == null) {
      if (other.host != null) {
        return false;
      }
    } else if (!host.equals(other.host)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (port != other.port) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Member [id=").append(id).append(", host=").append(host).append(", port=")
        .append(port).append("]");
    return builder.toString();
  }

}
