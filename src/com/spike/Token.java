package com.spike;

class Token {
  final int line;
  final int column; // starting symbol of this token
  final String lexeme;
  final Object literal;
  final TokenType type;

  Token(TokenType type, String lexeme, Object literal, int line, int column) {
    this.line = line;
    this.column = column;
    this.lexeme = lexeme;
    this.literal = literal;
    this.type = type;
  }

  @Override
  public String toString() {
    return "[" + lexeme + " " + type + " " + line + "]";
  }
}
