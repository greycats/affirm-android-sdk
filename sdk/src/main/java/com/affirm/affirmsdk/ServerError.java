package com.affirm.affirmsdk;

import com.affirm.affirmsdk.models.ErrorResponse;

public class ServerError extends Exception {
  private final ErrorResponse errorResponse;

  public ServerError(ErrorResponse errorResponse) {
    this.errorResponse = errorResponse;
  }

  public ErrorResponse getErrorResponse() {
    return errorResponse;
  }

  @Override public String toString() {
    return getErrorResponse().toString();
  }
}
