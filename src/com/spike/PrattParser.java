package com.spike;

import java.util.List;

class PrattParser implements Parser {
  List<Token> tokens;

  PrattParser(List<Token> tokens) {
    this.tokens = tokens;
  }

  @Override
  public List<Stmt> parse() {
    return null;
  }
}
