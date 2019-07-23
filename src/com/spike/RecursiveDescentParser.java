package com.spike;

import java.util.List;

class RecursiveDescentParser implements Parser {
  String source;

  RecursiveDescentParser(String source) {
    this.source = source;
  }

  @Override
  public List<Stmt> parse(List<Token> tokens) {
    return null;
  }
}
