package com.spike;

class Return extends RuntimeException {
  final Object value;

  Return(Object value) {
    this.value = value;
  }
}
