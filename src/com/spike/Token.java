package com.spike;

class Token {
  final int line;
  final int column; // starting symbol of this token
  final String lexeme;
  final Object literal;
  final TokenType type;

  Token(int line, int column, String lexeme, Object literal, TokenType type) {
    this.line = line;
    this.column = column;
    this.lexeme = lexeme;
    this.literal = literal;
    this.type = type;
  }

  @Override
  public String toString() {
    return "[" + lexeme + "," + type + "]";
  }
}
