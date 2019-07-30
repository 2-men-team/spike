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

  var i: int = 0;
  for (i = 0; i < 10; i = i + 1) {
    print("Iteration #" + i);
  }

  i = 0;
  while (i < 10) {
    print("Iteration with while #" + i);
    i = i + 1;
  }

  if (i >= 10) {
    print("Last value of i: " + i);
  }
}