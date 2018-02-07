package org.deer.vertx.cluster.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public interface Clustered {

  static void startClusteredVertx(Handler<AsyncResult<Vertx>> handler) {
    Vertx.factory.clusteredVertx(defaultOptions(),
        handler);
  }

  static VertxOptions defaultOptions() {
    return new VertxOptions().setClusterManager(new HazelcastClusterManager());
  }
}
