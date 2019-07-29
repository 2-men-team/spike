package com.spike;

import java.util.ArrayList;
import java.util.List;

import static com.spike.TokenType.*;

class RecursiveDescentParser extends Parser {
  private List<Token> tokens;
  private int curr;
  private List<Stmt> ast;

  RecursiveDescentParser(List<Token> tokens, ErrorReporter reporter) {
    super(reporter);
    this.tokens = tokens;
    this.curr = 0;
    this.ast = new ArrayList<>();
  }

  private boolean match(TokenType type) {
    if (!is(type)) return false;
    advance();
    return true;
  }

  private Token advance() {
    if (is(EOF)) return peek();
    return tokens.get(curr++);
  }

  private Token previous() {
    return tokens.get(curr - 1);
  }

  private Token peek() {
    return tokens.get(curr);
  }

  private Token peekNext() {
    if (is(EOF)) return peek();
    return tokens.get(curr + 1);
  }

  private boolean check(Token token, TokenType... types) {
    for (var type : types) {
      if (token.type == type) {
        return true;
      }
    }

    return false;
  }

  private boolean is(TokenType... types) {
    return check(peek(), types);
  }

  private boolean isNext(TokenType... types) {
    return check(peekNext(), types);
  }

  private Token consume(TokenType type, String msg) {
    if (!is(type)) {
      error(peek(), msg);
    }

    return advance();
  }

  private void error(Token token, String message) {
    reporter.panic(token, message);
  }

  private void synchronize() {
    while (!is(VAR, FUNCTION, FOR, WHILE, IF, EOF, SEMICOLON)) {
      advance();
    }

    if (is(SEMICOLON)) advance();
  }

  @Override
  public boolean parse() {
    while (!is(EOF)) {
      ast.add(declaration());
    }

    return !reporter.hadErrors();
  }

  @Override
  public List<Stmt> getAst() {
    return ast;
  }

  private Stmt declaration() {
    try {
      if (match(VAR)) return varDecl();
      if (match(FUNCTION)) return funDecl();
      return stmt();
    } catch (ErrorReporter.Exception e) {
      synchronize();
    }

    return null; // we don't care in case of an error
  }

  private Expr primary() {
    Token token = advance();

    switch (token.type) {
      case TRUE: return new Expr.Literal(true);
      case FALSE: return new Expr.Literal(false);
      case TOKEN_NULL: return new Expr.Literal(null);
      case INTEGER:
      case DOUBLE: return new Expr.Literal(token.literal);
      case IDENTIFIER: return new Expr.Variable(token);
      case STRING: return new Expr.StringLiteral((String) token.literal);
      case LEFT_PAREN:
        Expr value = expr();
        consume(RIGHT_PAREN, "')' is expected after grouping expression");
        return value;
      default:
        error(token, "Unexpected identifier");
    }

    throw new AssertionError("Unreachable");
  }

  private Expr call() {
    Expr value = primary();

    if (match(LEFT_PAREN)) {
      List<Expr> args = new ArrayList<>();

      if (!is(RIGHT_PAREN)) {
        do {
          args.add(expr());
        } while (match(COMMA));
      }

      Token paren = consume(RIGHT_PAREN, "')' is missing after call expression");
      return new Expr.Call(value, paren, args);
    }

    return value;
  }

  private Expr unary() {
    if (!is(NOT, MINUS, BIT_COMPL)) {
      return call();
    }

    return new Expr.Unary(advance(), unary());
  }

  // TODO: reduce boilerplate
  private Expr multiplication() {
    Expr left = unary();

    while (is(SLASH, STAR, REMAINDER)) {
      left = new Expr.Binary(left, advance(), unary());
    }

    return left;
  }

  private Expr addition() {
    Expr left = multiplication();

    while (is(PLUS, MINUS)) {
      left = new Expr.Binary(left, advance(), multiplication());
    }

    return left;
  }

  private Expr shift() {
    Expr left = addition();

    while (is(BIT_LEFT, BIT_RIGHT)) {
      left = new Expr.Binary(left, advance(), addition());
    }

    return left;
  }

  private Expr comparison() {
    Expr left = shift();

    while (is(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
      left = new Expr.Binary(left, advance(), shift());
    }

    return left;
  }

  private Expr equality() {
    Expr left = comparison();

    while (is(EQUAL_EQUAL, NOT_EQUAL)) {
      left = new Expr.Binary(left, advance(), comparison());
    }

    return left;
  }

  private Expr bitwiseAnd() {
    Expr left = equality();

    while (is(BIT_AND)) {
      left = new Expr.Binary(left, advance(), equality());
    }

    return left;
  }

  private Expr bitwiseXor() {
    Expr left = bitwiseAnd();

    while (is(BIT_XOR)) {
      left = new Expr.Binary(left, advance(), bitwiseAnd());
    }

    return left;
  }

  private Expr bitwiseOr() {
    Expr left = bitwiseXor();

    while (is(BIT_OR)) {
      left = new Expr.Binary(left, advance(), bitwiseXor());
    }

    return left;
  }

  private Expr logicalAnd() {
    Expr left = bitwiseOr();

    while (is(AND)) {
      left = new Expr.Binary(left, advance(), bitwiseOr());
    }

    return left;
  }

  private Expr logicalOr() {
    Expr left = logicalAnd();

    while (is(OR)) {
      left = new Expr.Binary(left, advance(), logicalAnd());
    }

    return left;
  }

  // FIXME later when classes will be introduced
  private Expr assignment() {
    if (!isNext(
        EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, REMAINDER_EQUAL)) {
      return logicalOr();
    }

    Token name = consume(IDENTIFIER, "Assignment target is missing");
    Token op = advance();
    Expr value = assignment();

    if (op.type != EQUAL) {
      value = new Expr.Binary(new Expr.Variable(name), op, value);
    }

    return new Expr.Assign(name, value);
  }

  private Expr expr() {
    return assignment();
  }

  private Stmt exprStmt() {
    Expr value = expr();
    consume(SEMICOLON, "';' is missing after expression statement");
    return new Stmt.Expression(value);
  }

  private Stmt returnStmt() {
    Token operator = previous();
    Expr value = null;
    if (!is(SEMICOLON)) value = expr();
    consume(SEMICOLON, "';' is missing after 'return' statement");
    return new Stmt.Return(operator, value);
  }

  private Stmt ifStmt() {
    Token operator = previous();
    consume(LEFT_PAREN, "'(' is missing in an 'if' statement");
    Expr condition = expr();
    consume(RIGHT_PAREN, "')' is missing in an 'if' statement");

    Stmt thenBranch = stmt(), elseBranch = null;
    if (match(ELSE)) elseBranch = stmt();

    return new Stmt.If(operator, condition, thenBranch, elseBranch);
  }

  private Stmt forStmt() {
    Token operator = previous();
    consume(LEFT_PAREN, "'(' is missing in a 'for' loop");

    Stmt init = null;
    if (match(VAR)) init = varDecl();
    else if (!match(SEMICOLON)) init = exprStmt();

    Expr condition, iter = null;

    if (is(SEMICOLON)) condition = new Expr.Literal(true);
    else condition = expr();

    consume(SEMICOLON, "';' expected after condition expression in a 'for' loop");

    if (!is(RIGHT_PAREN)) iter = expr();
    consume(RIGHT_PAREN, "')' is missing in a 'for' loop");

    List<Stmt> whileBody = new ArrayList<>();
    whileBody.add(stmt());
    if (iter != null) whileBody.add(new Stmt.Expression(iter));

    List<Stmt> block = new ArrayList<>();
    if (init != null) block.add(init);
    block.add(new Stmt.While(operator, condition, new Stmt.Block(whileBody)));

    return new Stmt.Block(block);
  }

  private Stmt whileStmt() {
    Token operator = previous();
    consume(LEFT_PAREN, "'(' is missing in a 'while' loop");
    Expr condition = expr();
    consume(RIGHT_PAREN, "')' is missing in a 'while' loop");
    return new Stmt.While(operator, condition, stmt());
  }

  private List<Stmt> block() {
    List<Stmt> result = new ArrayList<>();

    while (!is(EOF, RIGHT_BRACE)) {
      result.add(declaration()); // #FIXME: allow function in function declaration? are functions first-class objects?
    }

    consume(RIGHT_BRACE, "'}' is missing in a block statement");

    return result;
  }

  private Stmt stmt() {
    try {
      if (match(FOR)) return forStmt();
      if (match(WHILE)) return whileStmt();
      if (match(IF)) return ifStmt();
      if (match(LEFT_BRACE)) return new Stmt.Block(block());
      if (match(RETURN)) return returnStmt();
      return exprStmt();
    } catch (ErrorReporter.Exception e) {
      synchronize();
    }

    return null;
  }

  private Stmt funDecl() {
    Token name = consume(IDENTIFIER, "Function name expected after 'def'");
    consume(LEFT_PAREN, "'(' expected after function name");

    var params = new ArrayList<Token>();
    var types = new ArrayList<Token>();

    if (!is(RIGHT_PAREN)) {
      do {
        params.add(consume(IDENTIFIER, "Parameter name is expected"));
        consume(COLON, "':' is expected after parameter name");
        types.add(consume(IDENTIFIER, "Type identifier is expected after ':'"));
      } while (match(COMMA));
    }

    consume(RIGHT_PAREN, "')' is expected");
    Token returnType;
    if (match(VOID))
      returnType = previous();
    else
      returnType = consume(IDENTIFIER, "Missing return type of a function");
    consume(LEFT_BRACE, "'{' expected after function signature"); // #FIXME: hear or in block()?
    return new Stmt.Function(name, params, types, returnType, block());
  }

  private Stmt varDecl() {
    Token name = consume(IDENTIFIER, "Variable name expected after 'var'");
    Token type = null;

    if (match(COLON)) {
      type = consume(IDENTIFIER, "Missing type identifier after ':'");
    }

    Expr init = null;
    if (match(EQUAL)) {
      init = expr();
    }

    consume(SEMICOLON, "';' expected after variable declaration");

    return new Stmt.Var(name, type, init);
  }
}
