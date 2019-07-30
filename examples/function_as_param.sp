def main() void {
  return;
}

// TODO: pass function as parameter Function<double, int>, last is return type
def apply(x: double, update: Function<double, int>) int {
  return update(x);
}