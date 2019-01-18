package com.github.leaderelection;

/**
 * Unified single exception that's thrown and handled by various components of Leader Election. The
 * idea is to use the code enum to encapsulate various error/exception conditions. That said, stack
 * traces, where available and desired, are not meant to be kept from users.
 */
public final class LeaderElectionException extends Exception {
  private static final long serialVersionUID = 1L;
  private final Code code;

  public LeaderElectionException(final Code code) {
    super(code.getDescription());
    this.code = code;
  }

  public LeaderElectionException(final Code code, final String message) {
    super(message);
    this.code = code;
  }

  public LeaderElectionException(final Code code, final Throwable throwable) {
    super(throwable);
    this.code = code;
  }

  public Code getCode() {
    return code;
  }

  public static enum Code {
    // 1.
    INTERRUPTED("One of the leader election operations was interrupted"),
    // n.
    UNKNOWN_FAILURE(
        "Leader election internal failure. Check exception stacktrace for more details of the failure");

    private String description;

    private Code(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

}
