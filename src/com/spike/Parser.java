package com.spike;

import java.util.List;

interface Parser {
  List<Stmt> parse();

  static Parser getParser(List<Token> tokens) {
    if (Spike.isRDP)
      return new RecursiveDescentParser(tokens);
    else
      return new PrattParser(tokens);
  }
}
