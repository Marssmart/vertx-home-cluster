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
import io.vertx.ext.web.handler.CorsHandler;
import java.util.stream.Collectors;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.common.NodeReporter;
import org.deer.vertx.cluster.common.dto.ClusterNode;

public class HttpServerVerticle extends AbstractVerticle {

  private NodeReporter nodeReporter;

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

    registerCorsHandler(router);

    registerOsInfoHandler(router, vertx);

    registerNetworkDeviceInfoHandler(router, vertx);
    registerNetworkDeviceFlagsHandler(router, vertx);
    registerNetworkDeviceAddressHandler(router, vertx);

    registerClusterNodesHandler(router, vertx);
    registerClusterNodeShutdownHandler(router, vertx);
    registerClusterNodePingHandler(router, vertx);

    httpServer.requestHandler(router::accept).listen(8444);

    nodeReporter = new NodeReporter(vertx);
    nodeReporter.reportNodeStarted(new ClusterNode().setName("http-node"));
  }

  @Override
  public void stop() throws Exception {
    nodeReporter.close();
  }

  private static void registerClusterNodePingHandler(Router router, Vertx vertx) {
    router.route("/api/v1/cluster/nodes/ping")
        .handler(
            event -> {
              final String pingAddress = event.request().params().get("ping-address");
              vertx.eventBus().send(pingAddress, null, replyEvent -> {
                if (replyEvent.succeeded()) {
                  event.response().end("Ping successfull to " + pingAddress);
                } else {
                  event.fail(replyEvent.cause());
                }
              });
            });
  }

  private static void registerClusterNodeShutdownHandler(Router router, Vertx vertx) {
    router.route("/api/v1/cluster/nodes/shutdown")
        .handler(
            event -> {
              final String shutdownAddress = event.request().params().get("shutdown-address");
              vertx.eventBus().send(shutdownAddress, null);
              event.response().end("Shutdown signal send to " + shutdownAddress);
            });
  }

  private static void registerClusterNodesHandler(Router router, Vertx vertx) {
    router.route("/api/v1/cluster/nodes")
        .handler(event -> new NodeReporter(vertx).reportRunningNodes()
            .setHandler(nodesResult -> {
              if (nodesResult.succeeded()) {
                event.response().end(new JsonArray(nodesResult.result()
                    .stream()
                    .map(JsonObject::mapFrom)
                    .collect(Collectors.toList())).toBuffer());
              } else {
                event.fail(nodesResult.cause());
              }
            }));
  }

  private static void registerNetworkDeviceAddressHandler(Router router, Vertx vertx) {
    router.route("/api/v1/network-device-info/:index/address")
        .handler(event -> vertx.eventBus().send("network-device-address-info",
            getIndexParam(event),
            jsonObjectToStringPretty(event)));
  }

  private static void registerNetworkDeviceFlagsHandler(Router router, Vertx vertx) {
    router.route("/api/v1/network-device-info/:index/flags")
        .handler(event -> vertx.eventBus().send("network-device-flags-info",
            getIndexParam(event),
            jsonObjectToStringPretty(event)));
  }

  private static void registerNetworkDeviceInfoHandler(Router router, Vertx vertx) {
    router.route("/api/v1/network-device-info")
        .handler(event -> vertx.eventBus().send("network-device-info", null,
            jsonArrayToStringPretty(event)));
  }

  private static void registerOsInfoHandler(Router router, Vertx vertx) {
    router.route("/api/v1/os-info")
        .handler(event -> vertx.eventBus().send("os-info", null,
            reply -> event.response()
                .end(JsonObject.class.cast(reply.result().body()).encodePrettily())));
  }

  private static void registerCorsHandler(Router router) {
    router.route().handler(CorsHandler.create("*")
        .allowedMethod(io.vertx.core.http.HttpMethod.GET)
        .allowedMethod(io.vertx.core.http.HttpMethod.POST)
        .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Request-Method")
        .allowedHeader("Access-Control-Allow-Credentials")
        .allowedHeader("Access-Control-Allow-Origin")
        .allowedHeader("Access-Control-Allow-Headers")
        .allowedHeader("Content-Type"));
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
