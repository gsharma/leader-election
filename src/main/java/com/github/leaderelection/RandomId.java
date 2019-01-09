package com.github.leaderelection;

import java.util.UUID;

/**
 * A random id provider relying on random Type 4 UUIDs.
 * 
 * @author gaurav
 */
public final class RandomId implements Id {
  private static final long serialVersionUID = 1L;

  private final UUID id = UUID.randomUUID();

  @Override
  public String getId() {
    return id.toString();
  }

  @Override
  public int compareTo(Id other) {
    return this.id.compareTo(((RandomId) other).id);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    if (!(obj instanceof RandomId)) {
      return false;
    }
    RandomId other = (RandomId) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

  public RandomId() {}

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[id=").append(id.toString()).append("]");
    return builder.toString();
  }

}
