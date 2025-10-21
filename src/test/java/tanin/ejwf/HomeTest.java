package tanin.ejwf;

import org.junit.jupiter.api.Test;

public class HomeTest extends Base {
  @Test
  void visitHome() throws InterruptedException {
    go("/");
    assertContains(elem("body").getText(), "Embeddable Java Web Framework");
  }
}
