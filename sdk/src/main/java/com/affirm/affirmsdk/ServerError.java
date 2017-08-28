package com.affirm.affirmsdk;

import com.affirm.affirmsdk.models.ErrorResponse;

public class ServerError extends Exception {
  private final ErrorResponse errorResponse;

  public ServerError(ErrorResponse errorResponse) {
    this.errorResponse = errorResponse;
  }

  @Override public String getMessage() {
    return errorResponse.message();
  }

  @Override public String toString() {
    return errorResponse.toString();
  }
}
