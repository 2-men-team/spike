package com.spike;

import java.io.OutputStream;
import java.io.PrintWriter;

// TODO: Make errors great again
class ErrorReporter {
  private final PrintWriter writer;
  private boolean hadErrors;
  private final String[] lines;

  class Exception extends RuntimeException {
    Exception() {
      super();
    }

    Exception(String message) {
      super(message);
    }
  }

  ErrorReporter(OutputStream stream, String source) {
    this.writer = new PrintWriter(stream);
    this.lines = source.split("\n");
    this.hadErrors = false;
  }

  ErrorReporter(String source) {
    this(System.err, source);
  }

  public boolean hadErrors() {
    return hadErrors;
  }

  public void resetErrors() {
    hadErrors = false;
  }

  protected String format(Token token, String message) {
    String s = String.format("Error at line %d, col %d\n\t", token.line, token.column);
    return s + lines[token.line - 1] + "\n\t" +
        " ".repeat(token.column - 1) + "^\n\t" +
        "-".repeat(token.column - 1) + "|\n\n" +
        message + "\n";
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
