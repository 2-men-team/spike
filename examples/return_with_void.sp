// Error: void-return in function returning non-void
def print(x: String) int {
    return;
}

var x: String = "String";

// OK
def main() void {
  print(x);
  return;
}