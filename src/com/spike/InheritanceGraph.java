package com.spike;

import java.util.ArrayList;
import java.util.List;

class InheritanceGraph {
  private int V;
  private int E;
  private ArrayList<Integer>[] adj;

  @SuppressWarnings("unchecked")
  InheritanceGraph(int V) {
    if (V <= 0) throw new IllegalArgumentException("V is less equal to zero");
    this.V = V;
    this.E = 0;
    adj = (ArrayList<Integer>[]) new ArrayList[V];
    for (int v = 0; v < V; v++) {
      adj[v] = new ArrayList<>();
    }
  }

  int V() {
    return V;
  }

  int E() {
    return E;
  }

  void addEdge(int v, int w) {
    validate(v);
    validate(w);
    adj[v].add(w);
  }

  Iterable<Integer> adj(int v) {
    validate(v);
    return adj[v];
  }

  // v <= w ?
  boolean conforms(int v, int w) {
    List<Integer> list = new ArrayList<>();
    while (v != 0) {
      list.add(v);
      v = adj[v].get(0);
    }

    list.add(0);
    return list.contains(w);
  }

  private void validate(int v) {
    if (v < 0 || v >= V)
      throw new IllegalArgumentException("Vertice " + v + " is invalid");
  }
}
