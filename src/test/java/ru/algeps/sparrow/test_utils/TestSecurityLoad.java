package ru.algeps.sparrow.test_utils;

import org.junit.jupiter.api.BeforeAll;
import ru.algeps.sparrow.util.security.SecurityContextLoad;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class TestSecurityLoad {
  @BeforeAll
  static void setUp() {
    new SecurityContextLoad() {};
    System.setProperty("javax.net.debug", "all");
    Logger.getLogger("").setLevel(Level.ALL);
  }
}
