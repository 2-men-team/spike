package com.spike;

import java.io.OutputStream;
import java.io.PrintWriter;

// TODO: Make errors great again
class ErrorReporter {
  private PrintWriter writer;
  private boolean hadErrors;

  class Exception extends RuntimeException {
    Exception() {
      super();
    }

    Exception(String message) {
      super(message);
    }
  }

  ErrorReporter(OutputStream stream) {
    this.writer = new PrintWriter(stream);
    this.hadErrors = false;
  }

  ErrorReporter() {
    this(System.err);
  }

  public boolean hadErrors() {
    return hadErrors;
  }

  public void resetErrors() {
    hadErrors = false;
  }

  // TODO: add column support
  protected String format(Token token, String message) {
    return String.format(
        "Error at line %d, token %s:\n\t%s", token.line, token.lexeme, message);
  }

  void report(Token token, String message) {
    hadErrors = true;
    writer.println(format(token, message));
  }

  void panic(Token token, String message) {
    report(token, message);
    throw new Exception(message);
  }
}
