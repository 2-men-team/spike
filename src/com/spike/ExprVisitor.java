package com.spike;

interface ExprVisitor<T> {
  T visit(Expr.Unary expr);
  T visit(Expr.Grouping expr);
  T visit(Expr.Assign expr);
  T visit(Expr.Literal expr);
  T visit(Expr.Logical expr);
  T visit(Expr.StringLiteral expr);
  T visit(Expr.Call expr);
  T visit(Expr.Binary expr);
  T visit(Expr.Variable expr);
}
