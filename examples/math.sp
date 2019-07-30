def min(x: int, y: int) int {
    if (x < y) return x;
    return y;
}

def max(x: int, y: int) int {
    if (x > y) return x;
    return y;
}

def mult(x: int, y: int) int {
  return x * y;
}

def main() void {
  // print(1) is disallowed, as print takes 'String' as argument
  print("min(-100, 100) = " + min(-1100, 100));
  print("Multiplication: " + mult(-10, -25));
}