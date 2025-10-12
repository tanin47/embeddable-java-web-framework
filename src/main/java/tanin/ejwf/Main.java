package tanin.ejwf;


import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.Response;

import static com.renomad.minum.web.RequestLine.Method.GET;

public class Main {
    public static void main(String[] args) {
        var main = new Main();
        var minum = main.start(9090);
        minum.block();
    }

    public FullSystem start(int port) {
        var minum = MinumBuilder.build(port);
        var wf = minum.getWebFramework();

        wf.registerPath(
                GET,
                "",
                r -> {
                    String content = new String(Main.class.getResourceAsStream("/html/index.html").readAllBytes());
                    return Response.htmlOk(content);
                }
        );
        return minum;
    }
}
