package com.spike;

import java.util.List;

abstract class Expr {
  String type; // variable for type checker

  abstract <T> T accept(ExprVisitor<T> visitor);

  static class Unary extends Expr {
    final Token operator;
    final Expr right;

    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Binary extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Grouping extends Expr {
    final Expr expr;

    Grouping(Expr expr) {
      this.expr = expr;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Logical extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Literal extends Expr {
    Object value;

    Literal(Object value) {
      this.value = value;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class StringLiteral extends Expr {
    String value;

    StringLiteral(String value) {
      this.value = value;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Variable extends Expr {
    Token name;

    Variable(Token name) {
      this.name = name;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Assign extends Expr {
    final Token name;
    final Expr value;

    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Call extends Expr {
    final Expr callee;
    final Token paren;
    final List<Expr> args;

    Call(Expr callee, Token paren, List<Expr> args) {
      this.callee = callee;
      this.paren = paren;
      this.args = args;
    }

    @Override
    <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }
}
