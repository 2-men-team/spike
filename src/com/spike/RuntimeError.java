package com.spike;

class RuntimeError extends RuntimeException {
  final Token token;
  final String message;

  RuntimeError(Token token, String message) {
    super(message);
    this.token = token;
    this.message = message;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Token getToken() {
    return token;
  }
}
