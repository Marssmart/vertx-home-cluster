package org.deer.vertx.gui;

import com.google.common.io.Resources;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.common.NodeReporter;
import org.deer.vertx.cluster.common.dto.ClusterNode;

public class GuiVerticle extends AbstractVerticle {

  private static final boolean TEST = true;

  public static void main(String[] args) {

    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      if (TEST) {
        vertx.deployVerticle(new TestServerVerticle());
      }

      vertx.deployVerticle("org.deer.vertx.gui.GuiVerticle", deployEvent -> {
        new NodeReporter(vertx).reportNodeStarted(new ClusterNode().setName("gui-node"));
        try {
          Desktop.getDesktop().browse(new URI("http://localhost:80"));
        } catch (IOException | URISyntaxException e) {
          throw new IllegalStateException("Unable to browse", e);
        }
      });
    });

  }

  @Override
  public void start() throws Exception {

    final HttpServer server = vertx.createHttpServer();
    final Router router = Router.router(vertx);

    router.route("/")
        .handler(event -> {
          try {
            event.response().end(Resources.toString(Resources.getResource("index.html"),
                StandardCharsets.UTF_8));
          } catch (IOException e) {
            event.fail(e);
          }
        });

    router.route("/bower_components/*").handler(StaticHandler.create("static/bower_components"));
    router.route("/view/*").handler(StaticHandler.create("static/view"));

    server.requestHandler(router::accept).listen();
  }
}
