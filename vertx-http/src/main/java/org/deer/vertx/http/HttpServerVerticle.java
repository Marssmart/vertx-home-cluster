/*
 * Copyright 2018 Ján Srniček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.deer.vertx.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.deer.vertx.cluster.common.Clustered;

public class HttpServerVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      vertx.deployVerticle("org.deer.vertx.http.HttpServerVerticle");
    });
  }

  @Override
  public void start() throws Exception {
    final HttpServer httpServer = vertx.createHttpServer();

    final Router router = Router.router(vertx);

    router.route("/api/v1/os-info")
        .handler(event -> vertx.eventBus().send("os-info", null,
            reply -> event.response()
                .end(JsonObject.class.cast(reply.result().body()).encodePrettily())));

    router.route("/api/v1/network-device-info")
        .handler(event -> vertx.eventBus().send("network-device-info", null,
            jsonArrayToStringPretty(event)));

    router.route("/api/v1/network-device-info/:index/flags")
        .handler(event -> vertx.eventBus().send("network-device-flags-info",
            getIndexParam(event),
            jsonObjectToStringPretty(event)));

    router.route("/api/v1/network-device-info/:index/address")
        .handler(event -> vertx.eventBus().send("network-device-address-info",
            getIndexParam(event),
            jsonObjectToStringPretty(event)));

    httpServer.requestHandler(router::accept).listen(8444);
  }

  private static Handler<AsyncResult<Message<Object>>> jsonArrayToStringPretty(
      RoutingContext event) {
    return reply -> event.response()
        .end(JsonArray.class.cast(reply.result().body()).encodePrettily());
  }

  private static int getIndexParam(RoutingContext event) {
    return Integer.parseInt(event.request().getParam("index"));
  }

  private static Handler<AsyncResult<Message<Object>>> jsonObjectToStringPretty(
      RoutingContext event) {
    return reply -> {
      event.response()
          .end(JsonObject.class.cast(reply.result().body()).encodePrettily());
    };
  }
}
