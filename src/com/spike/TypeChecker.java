package com.spike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.spike.DefTypes.*;

class TypeChecker implements ExprVisitor<Integer>, StmtVisitor<Void> {
  private final ErrorReporter reporter;
  private final List<Stmt> stmts;
  private Environment environment = new Environment();

  private HashMap<String, Integer> classTable;
  private HashMap<Integer, String> intToClass;
  private InheritanceGraph tree;

  private boolean hasMain = false;
  private Stmt.Function currentFunction = null;

  TypeChecker(List<Stmt> stmts, ErrorReporter reporter) {
    this.stmts = stmts;
    this.reporter = reporter;
  }

  // #TODO: Better handle 'null' type
  private void buildClassTable() {
    classTable = new HashMap<>();
    classTable.put("Object", OBJECT);
    classTable.put("int", INT);
    classTable.put("double", DOUBLE);
    classTable.put("bool", BOOL);
    classTable.put("String", STRING);
    classTable.put("void", VOID);

    intToClass = new HashMap<>();
    intToClass.put(NULL, "Null");
    intToClass.put(OBJECT, "Object");
    intToClass.put(INT, "int");
    intToClass.put(DOUBLE, "double");
    intToClass.put(BOOL, "bool");
    intToClass.put(STRING, "String");
    intToClass.put(VOID, "void");
  }

  private void buildInheritanceTree() {
    tree = new InheritanceGraph(5);
    tree.addEdge(INT, OBJECT);
    tree.addEdge(DOUBLE, OBJECT);
    tree.addEdge(BOOL, OBJECT);
    tree.addEdge(STRING, OBJECT);
  }

  private void checkAll() {
    buildClassTable();
    buildInheritanceTree();
    builtins();
    if (reporter.hadErrors()) return;
    gatherGlobals();
    if (reporter.hadErrors()) return;
    checkStmts();
  }

  void check() {
    checkAll();
  }

  private void builtins() {
    // define builtin
    environment.define("print", new Stmt.Function(
      new Token(TokenType.IDENTIFIER, "print", null, -1, -1),
      Arrays.asList(new Token(TokenType.IDENTIFIER, "s", null, -1, -1)),
      Arrays.asList(new Token(TokenType.IDENTIFIER, "String", null, -1, -1)),
      new Token(TokenType.VOID, "void", null, -1, -1),
      new ArrayList<>()
    ));
  }

  private void gatherGlobals() {
    for (Stmt stmt : stmts) {
      if (stmt instanceof Stmt.Function) {
        Stmt.Function function = (Stmt.Function) stmt;
        if (function.name.lexeme.equals("main"))
          checkMainDecl(function);
        else
          checkFuncDecl(function);
      } else if (stmt instanceof Stmt.Var) {
        Stmt.Var v = (Stmt.Var) stmt;
        checkVarDecl(v);
      } else {
        reporter.report("Only variable and function declarations are allowed on top level " + stmt);
      }
    }

    if (!hasMain) {
      reporter.report("'main' method is not defined");
    }
  }

  private void checkFuncDecl(Stmt.Function function) {
    if (environment.probe(function.name)) {
      error(function.name, "'" + function.name.lexeme + "' is already defined"); // #TODO Add prev declaration
    } else {
      checkSignature(function.name, function.params, function.types, function.returnType);
      environment.define(function.name.lexeme, function);
    }
  }

  private void checkVarDecl(Stmt.Var variable) {
    checkType(variable.type);
  }

  private void checkMainDecl(Stmt.Function main) {
    if (hasMain) {
      Stmt.Function prevMain = (Stmt.Function) environment.get(main.name);
      error(main.name, "'main' is already defined, previous declaration at main:"
        + prevMain.name.line + ":" + prevMain.name.column);
    } else {
      if (main.params.size() != 0)
        error(main.name, "'main' takes zero arguments, but " + main.params.size() + " found");
      environment.define(main.name.lexeme, main);
      hasMain = true;
    }
  }

  private void checkSignature(Token functionName, List<Token> params, List<Token> types, Token returnType) {
    beginScope();

    checkTypes(types);
    checkType(returnType);

    for (Token param : params) {
      if (environment.probe(param)) {
        Token prev = (Token) environment.get(param);
        error(param, "Parameter '" + param.lexeme + "' is already defined at "
          + functionName.lexeme + ":" + prev.line + ":" + prev.column);
      } else {
        environment.define(param.lexeme, param);
      }
    }

    endScope();
  }

  private void checkStmts() {
    for (Stmt stmt : stmts) {
      if (stmt instanceof Stmt.Var) { // FIXME: avoid bindings inside functions problems
        stmt.accept(this);
      }
    }

    for (Stmt stmt : stmts) {
      if (!(stmt instanceof Stmt.Var)) {
        stmt.accept(this);
      }
    }
  }

  @Override
  public Void visit(Stmt.Var stmt) {
    if (environment.probe(stmt.name)) {
      error(stmt.name, "'" + stmt.name.lexeme + "' is already defined"); // #TODO Add prev declaration
    } else {
      environment.define(stmt.name.lexeme, stmt.type);
      int type = (stmt.init == null) ? NULL : stmt.init.accept(this);
      if (type != NULL && type != classTable.get(stmt.type.lexeme)) {
        error(stmt.name, "Type of expression '" + intToClass.get(type) + "' doesn't conform to declared type '"
          + stmt.type.lexeme + "'");
      }
    }

    return null;
  }

  @Override
  public Void visit(Stmt.Block stmt) {
    for (Stmt s : stmt.statements)
      s.accept(this);
    return null;
  }

  @Override
  public Void visit(Stmt.Expression stmt) {
    stmt.expr.accept(this);
    return null;
  }

  @Override
  public Void visit(Stmt.While stmt) {
    beginScope();

    int type = stmt.cond.accept(this);
    if (type != NULL && type != BOOL)
      error(stmt.operator, "While conditional expression expects 'bool', but '"
        + intToClass.get(type) + "' found");
    stmt.body.accept(this);

    endScope();
    return null;
  }

  @Override
  public Void visit(Stmt.If stmt) {
    beginScope();

    int type = stmt.cond.accept(this);
    if (type != NULL && type != BOOL) {
      error(stmt.operator, "If conditional expression expects 'bool' but '"
        + intToClass.get(type) + "' found");
    } else {
      stmt.thenBranch.accept(this);
      if (stmt.elseBranch != null)
        stmt.elseBranch.accept(this);
    }

    endScope();
    return null;
  }

  @Override
  public Void visit(Stmt.Return stmt) {
    if (stmt.expr == null) {
      if (currentFunction.returnType.type != TokenType.VOID)
        error(stmt.operator, "Empty return statement in function '" + currentFunction.name.lexeme + "' returning non void");
      return null;
    }

    int type = stmt.expr.accept(this);
    if (type != NULL && type != classTable.get(currentFunction.returnType.lexeme)) {
      error(stmt.operator, "Inferred type of return expression '" + intToClass.get(type) + "'"
       + " does not conform to declared type '" + currentFunction.returnType.lexeme
       + "' of function '" + currentFunction.name.lexeme + "'");
    }

    return null;
  }

  @Override
  public Void visit(Stmt.Function function) {
    beginScope();
    currentFunction = function;

    for (int i = 0; i < function.params.size(); i++)
      environment.define(function.params.get(i).lexeme, function.types.get(i));

    for (Stmt stmt : function.body)
      stmt.accept(this);

    new ReturnTypeChecker(function).check();

    currentFunction = null;
    endScope();
    return null;
  }

  @Override
  public Integer visit(Expr.Unary expr) {
    int type = expr.right.accept(this);

    switch (expr.operator.type) {
      case NOT: {
        if (type == BOOL)
          return expr.type = BOOL;
        error(expr.operator, "Boolean expected as argument to '!' expression");
        break;
      }
      case MINUS: {
        if (type == INT || type == DOUBLE)
          return expr.type = type;
        error(expr.operator, "Minus operator is defined only for 'int' and 'double' types");
        break;
      }
      case BIT_COMPL: {
        if (type == INT)
          return expr.type = type;
        error(expr.operator, "Bit complement operator is defined only for 'int");
        break;
      }
    }

    return expr.type = OBJECT;
  }

  @Override
  public Integer visit(Expr.Grouping expr) {
    return expr.type = expr.accept(this);
  }

  @Override
  public Integer visit(Expr.Assign expr) {
    int type = expr.value.accept(this);
    Object variable = environment.get(expr.name);
    if (variable instanceof Token) {
      String varType = ((Token) variable).lexeme;
      if (type == NULL || classTable.get(varType) == type)
        return expr.type = classTable.get(varType);
      error(expr.name, "Inferred type of expression '" + intToClass.get(type) + "'"
       + " does not conform to declared type '" + varType + "'" + " of the variable '" + expr.name.lexeme + "'");
    } else {
      error(expr.name, "Variable '" + expr.name.lexeme + "' is not defined.");
    }

    return expr.type = OBJECT;
  }

  @Override
  public Integer visit(Expr.Literal expr) {
    if (expr.value instanceof Boolean)
      return expr.type = BOOL;
    else if (expr.value instanceof Integer)
      return expr.type = INT;
    else if (expr.value instanceof Double)
      return expr.type = DOUBLE;
    return expr.type = NULL; // null value
  }

  @Override
  public Integer visit(Expr.StringLiteral expr) {
    return expr.type = STRING;
  }

  @Override
  public Integer visit(Expr.Call expr) {
    Token name = ((Expr.Variable) expr.callee).name;
    Object callee = environment.get(name);

    if (callee instanceof Stmt.Function) {
      Stmt.Function function = (Stmt.Function) callee;
      if (function.params.size() != expr.args.size()) {
        error(expr.paren, "Invalid number of arguments to '" + function.name.lexeme + "', "
         + function.params.size() + " expected, but found " + expr.args.size());
      } else {
        List<Integer> types = new ArrayList<>();
        for (int i = 0; i < expr.args.size(); i++)
          types.add(expr.args.get(i).accept(this));

        for (int i = 0; i < expr.args.size(); i++) {
          int type = types.get(i);
          if (type != NULL && type != classTable.get(function.types.get(i).lexeme)) {
            error(expr.paren, "Inferred type '" + intToClass.get(type) + "' of parameter '"
              + function.params.get(i) + "' does not correspond to declared type '"
              + function.types.get(i).lexeme + "'");
          }
        }
      }

      return expr.type = classTable.get(function.returnType.lexeme);
    } else {
      error(expr.paren, "Undefined reference to the function");
    }

    return expr.type = OBJECT;
  }

  @Override
  public Integer visit(Expr.Binary expr) {
    int left = expr.left.accept(this), right = expr.right.accept(this);
    left = (left == NULL) ? right : left;
    right = (right == NULL) ? left : right;

    if (expr.operator.type == TokenType.PLUS && (left == STRING || right == STRING))
      return expr.type = STRING; // concat everything for string

    if (left != right) {
      error(expr.operator, "Operator '" + expr.operator.lexeme + "' cannot be applied to '"
        + intToClass.get(left) + "' and '" + intToClass.get(right) + "'");
      return expr.type = OBJECT;
    }

    switch (expr.operator.type) {
      case LESS:
      case LESS_EQUAL:
      case GREATER:
      case GREATER_EQUAL: {
        if (left == INT || left == DOUBLE)
          return expr.type = BOOL;
        break;
      }
      case PLUS:
      case MINUS:
      case SLASH:
      case STAR: {
        if (left == INT || left == DOUBLE)
          return expr.type = left;
        break;
      }
      case REMAINDER:
      case BIT_LEFT:
      case BIT_RIGHT:
      case BIT_AND:
      case BIT_XOR:
      case BIT_OR: {
        if (left == INT)
          return expr.type = left;
        break;
      }
      case EQUAL_EQUAL:
      case NOT_EQUAL:
        return expr.type = BOOL;
      case AND:
      case OR: {
        if (left == BOOL)
          return expr.type = BOOL;
        break;
      }
    }

    error(expr.operator, "Operator '" + expr.operator.lexeme + "' cannot be applied to '"
      + intToClass.get(left) + "' and '" + intToClass.get(right) + "'");
    return expr.type = OBJECT;
  }

  @Override
  public Integer visit(Expr.Variable expr) {
    Object variable = environment.get(expr.name);
    if (variable instanceof Token) {
      return expr.type = classTable.get(((Token) variable).lexeme);
    }

    error(expr.name, "Variable '" + expr.name.lexeme + "' is not defined");
    return expr.type = OBJECT;
  }

  private void checkTypes(List<Token> types) {
    for (Token type : types) {
      checkType(type);
    }
  }

  private void checkType(Token type) {
    if (!classTable.containsKey(type.lexeme)) {
      error(type, "Type '" + type.lexeme + "' is not defined");
    }
  }

  private class ReturnTypeChecker implements StmtVisitor<Boolean> {
    private Stmt.Function function;
    private Stmt.Return lastReturn;
    private boolean hadError = false;

    ReturnTypeChecker(Stmt.Function function) {
      this.function = function;
    }

    boolean check() {
      if (checkReturnStmts(function.body))
        return true;

      if (!hadError && function.returnType.type != TokenType.VOID) {
        error(function.name, "Control reached the end in function returning non-void");
        return false;
      }

      return !hadError;
    }

    private boolean checkReturnStmts(List<Stmt> statements) {
      for (int i = 0; i < statements.size(); i++) {
        boolean willReturn = statements.get(i).accept(this);
        if (willReturn) {
          if (i == statements.size() - 1) return true;
          else {
            hadError = true;
            error(lastReturn.operator,"Unreachable statement(s) after 'return'"); // #TODO: improve unreachability error handling
          }
        }
      }

      return false;
    }

    @Override
    public Boolean visit(Stmt.Var stmt) {
      return false;
    }

    @Override
    public Boolean visit(Stmt.Block stmt) {
      return checkReturnStmts(stmt.statements);
    }

    @Override
    public Boolean visit(Stmt.Expression stmt) {
      return false;
    }

    @Override
    public Boolean visit(Stmt.While stmt) {
      return stmt.body.accept(this);
    }

    @Override
    public Boolean visit(Stmt.If stmt) {
      return stmt.thenBranch.accept(this) && ((stmt.elseBranch == null) ?
        false : stmt.elseBranch.accept(this));
    }

    @Override
    public Boolean visit(Stmt.Return stmt) {
      lastReturn = stmt;
      return true;
    }

    @Override
    public Boolean visit(Stmt.Function stmt) {
      return null;
    }
  }

  public HashMap<String, Integer> getClassTable() {
    return classTable;
  }

  public HashMap<Integer, String> getIntToClass() {
    return intToClass;
  }

  private void error(Token token, String message) {
    reporter.report(token, message);
  }

  private void beginScope() {
    environment = new Environment(environment);
  }

  private void endScope() {
    environment = environment.eclosing;
  }
}
