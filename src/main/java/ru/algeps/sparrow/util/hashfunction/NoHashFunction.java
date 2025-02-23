package ru.algeps.sparrow.util.hashfunction;

import java.util.Arrays;

public class NoHashFunction implements HashFunction {
  @Override
  public byte[] hash(byte[] val) {
    return val;
  }

  @Override
  public boolean match(byte[] raw, byte[] hashing) {
    return Arrays.equals(raw, hashing);
  }
}
