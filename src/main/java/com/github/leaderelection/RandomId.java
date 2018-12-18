package com.github.leaderelection;

import java.util.UUID;

/**
 * A random id provider relying on random Type 4 UUIDs.
 * 
 * @author gaurav
 */
public final class RandomId implements Id {
  private final UUID id = UUID.randomUUID();

  @Override
  public String id() {
    return id.toString();
  }

  @Override
  public int compareTo(String other) {
    return this.id.compareTo(UUID.fromString(other));
  }

}
