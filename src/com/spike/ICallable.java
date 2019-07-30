package com.spike;

import java.util.List;

interface ICallable {
  Object call(Interpreter interpreter, List<Object> arguments);
}
