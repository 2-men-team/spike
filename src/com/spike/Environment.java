package com.spike;

import java.util.HashMap;

class Environment {
  final Environment eclosing;
  private final HashMap<String, Object> values = new HashMap<>();

  Environment() {
    this(null);
  }

  Environment(Environment eclosing) {
    this.eclosing = eclosing;
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    if (eclosing != null) {
      return eclosing.get(name);
    }

    return null;
  }

  void define(String name, Object value) {
    values.put(name, value);
  }

  boolean contains(Token name) {
    return get(name) != null;
  }

  boolean probe(Token name) {
    return values.containsKey(name.lexeme);
  }

  Environment ancestor(int distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++)
      environment = environment.eclosing;
    return environment;
  }

  Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }

  void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  Environment getEclosing() {
    return eclosing;
  }

  public HashMap<String, Object> getValues() {
    return values;
  }
}
