package com.spike;

import java.util.List;

interface Parser {
  List<Stmt> parse(List<Token> tokens);

  static Parser getParser(String source) {
    if (Spike.isRDP)
      return new RecursiveDescentParser(source);
    else
      return new PrattParser(source);
  }
}
