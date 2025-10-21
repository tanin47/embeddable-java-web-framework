package tanin.ejwf;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Base {
  static int PORT = 9091;
  static boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

  WebDriver webDriver;
  Main server;
  Connection conn;
  boolean shouldLoggedIn = true;

  @BeforeAll
  void setUpAll() throws SQLException, URISyntaxException, InterruptedException {
    initializeWebDriver();
  }

  public void initializeWebDriver() {
    if (webDriver != null) {
      webDriver.close();
    }
    var options = new ChromeOptions();

    options.addArguments("--guest");
    options.addArguments("--disable-extensions");
    options.addArguments("--disable-web-security");
    options.addArguments("--window-size=1280,800");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-smooth-scrolling");

    var logPrefs = new LoggingPreferences();
    logPrefs.enable(LogType.BROWSER, Level.ALL);
    options.setCapability("goog:loggingPrefs", logPrefs);

    webDriver = new ChromeDriver(options);
  }

  @BeforeEach
  void setUp() throws SQLException, URISyntaxException {
    server = new Main(PORT);
    server.start();
  }

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop();
    }
  }

  @AfterAll
  void tearDownAll() throws SQLException {
    webDriver.close();
  }

  void go(String path) {
    webDriver.get("http://localhost:" + PORT + path);
  }

  String tid(String... args) {
    var beginning = true;
    var sb = new StringBuilder();

    for (int i = 0; i < args.length; i++) {
      if (beginning) {
        sb.append("[data-test-id=")
          .append(args[i])
          .append("]");
        beginning = false;
      } else if (args[i] == null) {
        sb.append(" ");
        beginning = true;
      } else {
        sb.append("[data-test-value=")
          .append(args[i])
          .append("]");
      }
    }

    return sb.toString();
  }

  WebElement elem(String cssSelector) throws InterruptedException {
    return elem(cssSelector, true);
  }

  WebElement elem(String cssSelector, boolean checkDisplay) throws InterruptedException {
    if (checkDisplay) {
      waitUntil(() -> assertTrue(elems(cssSelector).stream().anyMatch(WebElement::isDisplayed)));
      return elems(cssSelector).stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
    } else {
      return elems(cssSelector).getFirst();
    }
  }

  boolean hasElem(String cssSelector) throws InterruptedException {
    return webDriver.findElements(new By.ByCssSelector(cssSelector)).stream().anyMatch(WebElement::isDisplayed);
  }

  List<WebElement> elems(String cssSelector) throws InterruptedException {
    waitUntil(() -> assertFalse(webDriver.findElements(new By.ByCssSelector(cssSelector)).isEmpty()));
    return webDriver.findElements(new By.ByCssSelector(cssSelector));
  }

  void retryInteraction(VoidFn fn) throws InterruptedException {
    for (int retry = 0; retry < 2; retry++) {
      try {
        fn.invoke();
        return;
      } catch (ElementNotInteractableException ex) {
        Thread.sleep(1000);
      }
    }

    // Last try
    fn.invoke();
  }

  void click(String cssSelector) throws InterruptedException {
    retryInteraction(() -> elem(cssSelector).click());
  }

  void hover(String cssSelector) throws InterruptedException {
    var actions = new Actions(webDriver);
    actions.moveToElement(elem(cssSelector), 2, 2).perform();
  }

  void fill(String cssSelector, String text) throws InterruptedException {
    retryInteraction(() -> {
      var el = elem(cssSelector);

      el.sendKeys(IS_MAC ? Keys.COMMAND : Keys.CONTROL, "a");
      el.sendKeys(Keys.BACK_SPACE);

      Thread.sleep(10);
      el.sendKeys(text);
    });
  }

  void sendClearKeys() throws InterruptedException {
    var actions = new Actions(webDriver);
    var modifierKey = IS_MAC ? Keys.COMMAND : Keys.CONTROL;
    actions
      .keyDown(modifierKey)
      .sendKeys("a")
      .keyUp(modifierKey)
      .sendKeys(Keys.BACK_SPACE)
      .perform();
  }

  void sendKeys(String text) {
    var actions = new Actions(webDriver);
    actions.sendKeys(text).perform();
  }


  int WAIT_UNTIL_TIMEOUT_MILLIS = 5000;

  @FunctionalInterface
  interface InterruptibleSupplier {
    boolean get() throws InterruptedException;
  }

  @FunctionalInterface
  interface VoidFn {
    void invoke() throws InterruptedException;
  }

  void waitUntil(VoidFn fn) throws InterruptedException {
    InterruptibleSupplier newFn = () -> {
      try {
        fn.invoke();
        return true;
      } catch (AssertionError | StaleElementReferenceException | java.util.NoSuchElementException |
               NoSuchElementException e) {
        return false;
      }
    };

    var startTime = System.currentTimeMillis();
    while ((System.currentTimeMillis() - startTime) < WAIT_UNTIL_TIMEOUT_MILLIS) {
      if (newFn.get()) return;
      Thread.sleep(500);
    }

    fn.invoke();
  }

  void assertContains(String actual, String expectedPart) {
    assertTrue(actual.contains(expectedPart), () -> actual + " doesn't contain " + expectedPart);
  }
}
