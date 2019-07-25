package com.spike;

import java.util.List;

abstract class Parser {
  protected ErrorReporter reporter;

  Parser(ErrorReporter reporter) {
    this.reporter = reporter;
  }

  abstract List<Stmt> parse();

  public ErrorReporter getReporter() {
    return reporter;
  }

  static Parser getParser(List<Token> tokens, ErrorReporter reporter) {
    if (Spike.isRDP)
      return new RecursiveDescentParser(tokens, reporter);
    else
      return new PrattParser(tokens, reporter);
  }
}
