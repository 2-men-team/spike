package com.spike;

import java.util.List;

abstract class Stmt {
  abstract <T> T accept(StmtVisitor<T> visitor);

  static class Expression extends Stmt {
    final Expr expr;

    Expression(Expr expr) {
      this.expr = expr;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Var extends Stmt {
    final Token name;
    final Token type;
    final Expr init;

    Var(Token name, Token type, Expr init) {
      this.name = name;
      this.type = type;
      this.init = init;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class If extends Stmt {
    final Token operator;
    final Expr cond;
    final Stmt thenBranch;
    final Stmt elseBranch;

    If(Token operator, Expr cond, Stmt thenBranch, Stmt elseBranch) {
      this.operator = operator;
      this.cond = cond;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Block extends Stmt {
    final List<Stmt> statements;

    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class While extends Stmt {
    final Token operator;
    final Expr cond;
    final Stmt body;

    While(Token operator, Expr cond, Stmt body) {
      this.operator = operator;
      this.cond = cond;
      this.body = body;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Function extends Stmt {
    final Token name;
    final List<Token> params;
    final List<Token> types;
    final Token returnType;
    final List<Stmt> body;

    Function(Token name, List<Token> params, List<Token> types, Token returnType, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.types = types;
      this.returnType = returnType;
      this.body = body;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  static class Return extends Stmt {
    final Token operator;
    final Expr expr;

    Return(Token operator, Expr expr) {
      this.operator = operator;
      this.expr = expr;
    }

    @Override
    <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }
}
