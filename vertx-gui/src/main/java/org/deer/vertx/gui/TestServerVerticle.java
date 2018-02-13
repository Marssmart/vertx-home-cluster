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

package org.deer.vertx.gui;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import java.util.Arrays;
import org.deer.vertx.cluster.common.dto.ClusterNode;

public class TestServerVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    final HttpServer server = vertx.createHttpServer();

    final Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*")
        .allowedMethod(io.vertx.core.http.HttpMethod.GET)
        .allowedMethod(io.vertx.core.http.HttpMethod.POST)
        .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
        .allowedHeader("Access-Control-Request-Method")
        .allowedHeader("Access-Control-Allow-Credentials")
        .allowedHeader("Access-Control-Allow-Origin")
        .allowedHeader("Access-Control-Allow-Headers")
        .allowedHeader("Content-Type"));

    router.route("/api/v1/cluster/nodes")
        .handler(event -> {
          final ClusterNode networkNode = new ClusterNode().setName("Network node");
          final ClusterNode sensorNode = new ClusterNode().setName("Sensor node");
          final ClusterNode serverNode = new ClusterNode().setName("Server node");
          final ClusterNode unreachableNode = new ClusterNode().setName("Unreachable node");

          event.response().end(
              new JsonArray(Arrays.asList(networkNode, sensorNode, serverNode, unreachableNode))
                  .toBuffer());
        });

    server.requestHandler(router::accept).listen(8444);
  }
}
