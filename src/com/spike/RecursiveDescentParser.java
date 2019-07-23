package com.spike;

import java.util.List;

class RecursiveDescentParser implements Parser {
  List<Token> tokens;

  RecursiveDescentParser(List<Token> tokens) {
    this.tokens = tokens;
  }

  @Override
  public List<Stmt> parse() {
    return null;
  }
}
