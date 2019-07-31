package com.spike;

class TString {
  private String string;

  public TString() {
    this("");
  }

  public TString(char c) {
    this("" + c);
  }

  public TString(TString t) {
    this(t.string);
  }

  public TString(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null) return false;
    if (obj.getClass() != this.getClass()) return false;
    TString that = (TString) obj;
    return this.string.equals(that.string);
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }
}
