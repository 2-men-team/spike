// OK
def main() void {
  return;
}

var y : String;

// Invalid type Error: 'double' does not conform to declared type 'int'
def sum(x: int, y: double) int {
  if (x == 5) {
    return x*2;
  } else {
    return y = 0.4;
  }
}

// No-return Error: Control reached the end ...
def if_stmt(x: int) int {
  if (x == 5) {
    return x*2;
  } else {
    x = 15;
  }
}

// No-return Error: Control reached the end ...
def while_stmt(x: String) String {
  while (true) {
    x = x + " buzz";
  }
}

// OK
def call(param: int) void {
  if (if_stmt(param) == 1) {
    return;
  } else {
    y = "No-return in function with 'void' is ok";
  }
}