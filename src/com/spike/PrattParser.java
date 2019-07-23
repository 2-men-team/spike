package com.spike;

import java.util.List;

class PrattParser implements Parser {
  String source;

  PrattParser(String source) {
    this.source = source;
  }

  @Override
  public List<Stmt> parse(List<Token> tokens) {
    return null;
  }
}
