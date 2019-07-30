package com.spike;

import java.util.ArrayList;
import java.util.List;

class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
  private final ErrorReporter reporter;
  private Environment environment =  new Environment();

  Interpreter(ErrorReporter reporter) {
    this.reporter = reporter;

    environment.define("print", (ICallable) (interpreter, arguments) -> {
      System.out.println(arguments.get(0));
      return null;
    });
  }

  public void interpret(List<Stmt> stmts) {
    //try {
      for (Stmt stmt : stmts) {
        if (stmt instanceof Stmt.Function) {
          stmt.accept(this);
        }
      }

      for (Stmt stmt : stmts) {
        if (!(stmt instanceof Stmt.Function)) {
          stmt.accept(this);
        }
      }

      ICallable main = (ICallable) environment.getByName("main");
      main.call(this, new ArrayList<>());
    //} catch (Exception e) {
      //reporter.report(e.getMessage());
    //}
  }

  @Override
  public Object visit(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case NOT:
        return !isTruthy(right);
      case MINUS:
        return (expr.right.type == 1) ? - (int) right : - (double) right;
      case BIT_COMPL:
        return ~ (int) right;
    }

    return null;
  }

  @Override
  public Object visit(Expr.Grouping expr) {
    return evaluate(expr.expr);
  }

  @Override
  public Object visit(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);
    return null;
  }

  @Override
  public Object visit(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visit(Expr.StringLiteral expr) {
    return new TString(expr.value);
  }

  @Override
  public Object visit(Expr.Call expr) {
    ICallable function = (ICallable) evaluate(expr.callee);
    List<Object> args = new ArrayList<>();
    for (Expr arg : expr.args) {
      Object val = evaluate(arg);
      if (val instanceof Double && (((Double) val) % 1) == 0) // #FIXME: bug, Integer automatically converts to Double, hence error
        val = ((Double) val).intValue();
      args.add(val);
    }

    return function.call(this, args);
  }

  @Override
  public Object visit(Expr.Binary expr) {
    Object left = evaluate(expr.left), right = evaluate(expr.right);
    int type = expr.left.type;

    switch (expr.operator.type) {
      case BIT_OR: return (int) left | (int) right;
      case BIT_XOR: return (int) left ^ (int) right;
      case BIT_LEFT: return (int) left << (int) right;
      case BIT_RIGHT: return (int) left >> (int) right;
      case BIT_AND: return (int) left & (int) right;
      case NOT_EQUAL: return !isEqual(left, right);
      case EQUAL_EQUAL: return isEqual(left, right);
      case GREATER: return (type == 1) ?
        (int) left > (int) right : (double) left > (double) right;
      case GREATER_EQUAL: return (type == 1) ?
        (int) left >= (int) right : (double) left >= (double) right;
      case LESS: return (type == 1) ?
        (int) left < (int) right : (double) left < (double) right;
      case LESS_EQUAL: return (type == 1) ?
        (int) left <= (int) right : (double) left <= (double) right;
      case PLUS: switch (type) {
        case 1: return (int) left + (int) right;
        case 2: return (double) left + (double) right;
        case 4: return new TString(left.toString() + right.toString());
      }
      case MINUS: return (type == 1) ?
        (int) left - (int) right : (double) left - (double) right;
      case STAR: return (type == 1) ?
        (int) left * (int) right : (double) left * (double) right;
      case SLASH: return (type == 1) ?
        (int) left / (int) right : (double) left / (double) right;
      case REMAINDER: return (int) left % (int) right;
      case OR: return (boolean) left || (boolean) right;
      case AND: return (boolean) left && (boolean) right;
    }

    return null;
  }

  @Override
  public Object visit(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  @Override
  public Void visit(Stmt.Var stmt) {
    Object value = null;
    if (stmt.init != null)
      value = evaluate(stmt.init);
    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visit(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visit(Stmt.Expression stmt) {
    evaluate(stmt.expr);
    return null;
  }

  @Override
  public Void visit(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.cond))) {
      execute(stmt.body);
    }

    return null;
  }

  @Override
  public Void visit(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.cond))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }

    return null;
  }

  @Override
  public Void visit(Stmt.Return stmt) {
    Object value = null;
    if (stmt.expr != null) value = evaluate(stmt.expr);
    throw new Return(value);
  }

  @Override
  public Void visit(Stmt.Function stmt) {
    TFunction function = new TFunction(stmt, environment);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  private boolean isTruthy(Object o) {
    if (o == null) return false;
    if (o instanceof Boolean) return (boolean) o;
    return true;
  }

  Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void executeBlock(List<Stmt> stmts, Environment environment) {
    Environment prev = this.environment;
    try {
      this.environment = environment;
      stmts.forEach(this::execute);
    } finally {
      this.environment = prev;
    }
  }
}
