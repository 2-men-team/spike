package com.spike;

import java.util.List;

class PrattParser extends Parser {
  List<Token> tokens;

  PrattParser(List<Token> tokens, ErrorReporter reporter) {
    super(reporter);
    this.tokens = tokens;
  }

  @Override
  public List<Stmt> parse() {
    return null;
  }
}
