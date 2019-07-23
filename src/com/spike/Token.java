package com.spike;

class Token {
  final int line;
  final String lexeme;
  final Object literal;
  final TokenType type;

  Token(int line, String lexeme, Object literal, TokenType type) {
    this.line = line;
    this.lexeme = lexeme;
    this.literal = literal;
    this.type = type;
  }

  @Override
  public String toString() {
    return "[" + lexeme + "," + type + "]";
  }
}
