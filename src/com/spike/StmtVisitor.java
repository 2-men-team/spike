package com.spike;

interface StmtVisitor<T> {
  T visit(Stmt.Var stmt);
  T visit(Stmt.Block stmt);
  T visit(Stmt.Expression stmt);
  T visit(Stmt.While stmt);
  T visit(Stmt.If stmt);
  T visit(Stmt.Return stmt);
  T visit(Stmt.Function stmt);
}
