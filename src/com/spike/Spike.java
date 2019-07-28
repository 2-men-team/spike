package com.spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// Java version 11
public final class Spike {
  private static final ErrorReporter reporter = new ErrorReporter();

  static boolean isRDP = true;
  /*
  private static void runCLI() throws IOException {
    var reader = new BufferedReader(new InputStreamReader(System.in));
    System.out.print(">> ");
    while (true) {
      String line = reader.readLine();
      if (line == null || line.equalsIgnoreCase("exit")) return;
      run(line);
      System.out.print(">> ");
    }
  }*/

  private static void runFile(String path) throws IOException {
    run(Files.readString(Paths.get(path)));
  }

  private static void run(String source) {
    reporter.setSource(source);
    var scanner = new Scanner(source, reporter);
    var tokens = scanner.scanTokens();

    Parser parser = Parser.getParser(tokens, reporter);
    if (!parser.parse()) return;

    List<Stmt> stmts = parser.getAst();
    TypeChecker checker = new TypeChecker(stmts, reporter, new Environment());
  }

  private static boolean setParserType(String name) {
    if (name.equalsIgnoreCase("pratt"))
      isRDP = false;
    else if (name.equalsIgnoreCase("rdf"))
      isRDP = true;
    else return false;
    return true;
  }

  // #TODO: REWRITE CLI INTERFACE
  public static void main(String... args) throws IOException {
    if (args.length == 3) {
      if (setParserType(args[2]))
        runFile(args[1]);
      else
        System.out.println("Invalid parser type, 'pratt' and 'rdp' supported only.");
    } else if (args.length == 2) {
      runFile(args[1]);
    } else {
      System.out.println("Usage: spike main.sp [ parser type ]");
    }
  }
}