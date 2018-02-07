package org.deer.vertx.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.deer.vertx.cluster.common.Clustered;

public class SensorVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      vertx.deployVerticle("org.deer.vertx.sensor.OsVerticle");
    });
  }
}
