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

  Object getByName(String lexeme) {
    if (values.containsKey(lexeme)) {
      return values.get(lexeme);
    }

    if (eclosing != null) {
      return eclosing.getByName(lexeme);
    }

    return null;
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (eclosing != null) {
      eclosing.assign(name, value);
    }
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

  Environment getEclosing() {
    return eclosing;
  }

  public HashMap<String, Object> getValues() {
    return values;
  }
}
