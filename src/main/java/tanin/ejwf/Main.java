package tanin.ejwf;


import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.StatusLine;

import java.io.IOException;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.renomad.minum.web.RequestLine.Method.GET;

public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getName());

  static {
    try (var configFile = Main.class.getResourceAsStream("/ejwf_default_logging.properties")) {
      LogManager.getLogManager().readConfiguration(configFile);
      logger.info("The log config (default_logging.properties) has been loaded.");
    } catch (IOException e) {
      logger.warning("Could not load the log config file (default_logging.properties): " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    var main = new Main(9090);
    main.start();
    main.minum.block();
  }

  int port;
  public FullSystem minum;

  Main(int port) {
    this.port = port;
  }

  public void start() {
    minum = MinumBuilder.build(port);
    var wf = minum.getWebFramework();

    wf.registerPath(
      GET,
      "",
      r -> {
        logger.info("Serve /");
        String content = new String(Main.class.getResourceAsStream("/html/index.html").readAllBytes());
        return Response.htmlOk(content);
      }
    );

    wf.registerPath(
      GET,
      "healthcheck",
      req -> {
        return Response.buildResponse(StatusLine.StatusCode.CODE_200_OK, Map.of("Content-Type", "text/plain"), "OK EWJF");
      }
    );
  }

  public void stop() {
    if (minum != null) {
      minum.shutdown();
    }
  }
}
