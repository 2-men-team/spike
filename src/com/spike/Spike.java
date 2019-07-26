package com.spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

// Java version 11
public final class Spike {
  static boolean isRDP = true;

  private static void runCLI() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    System.out.print(">> ");
    while (true) {
      String line = reader.readLine();
      if (line == null || line.equalsIgnoreCase("exit")) return;
      run(line);
      System.out.print(">> ");
    }
  }

  private static void runFile(String path) throws IOException {
    run(Files.readString(Paths.get(path)));
  }

  private static void run(String source) {
    System.out.println(source);
  }

  private static boolean setParserType(String name) {
    if (name.equalsIgnoreCase("pratt"))
      isRDP = false;
    else if (name.equalsIgnoreCase("rdf"))
      isRDP = true;
    else return false;
    return true;
  }

  // #TODO: REWRITE CLI INTERFACE (picocli)
  public static void main(String[] args) throws IOException {
    if (args.length > 3) {
      System.out.println("Usage: spike [ main.sp ] [ parser type ]");
    } else if (args.length == 3) {
      if (setParserType(args[2]))
        runFile(args[1]);
      else
        System.out.println("Invalid parser type, 'pratt' and 'rdp' supported only.");
    } else if (args.length == 2) {
      if (setParserType(args[1]))
        runCLI();
      else
        runFile(args[1]);
    } else {
      runCLI();
    }
  }
}