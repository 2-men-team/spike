package com.spike;

import java.io.OutputStream;
import java.io.PrintWriter;

// TODO: Make errors great again
class ErrorReporter {
  private final PrintWriter writer;
  private boolean hadErrors;
  private String[] lines;

  class Exception extends RuntimeException {
    Exception() {
      super();
    }

    Exception(String message) {
      super(message);
    }
  }

  ErrorReporter(OutputStream stream) {
    this.writer = new PrintWriter(stream, true);
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

  protected String format(int line, String message) {
    return String.format("Error at line %d:\n\t%s", line, message);
  }

  ErrorReporter setSource(String source) {
    lines = source.split("\n");
    return this;
  }

  void report(int line, String message) {
    hadErrors = true;
    writer.println(format(line, message));
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
