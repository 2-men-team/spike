def main() void {
  return;
}

def fn() String {
  return "below statements are unreachable";
  var x : String = "value";

  if (x == "value") {
    return "true";
  }
}