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

package org.deer.vertx.mma.rankings;

import static org.deer.vertx.mma.rankings.task.impl.PageRequestTask.PAGE_REQUEST_TASK;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;


public class TestHttpServerVerticle extends AbstractVerticle implements TaskSubmitter {

  @Override
  public void start() throws Exception {
    final HttpServer httpServer = vertx.createHttpServer();
    final Router router = Router.router(vertx);

    router.route("/api/v1/mma/scrape-fighter-profile")
        .handler(event -> {
          final MultiMap params = event.request().params();

          final String link = params.get("link");
          if (link != null) {
            final TaskDescription taskDescriptor = createTaskDescriptor(PAGE_REQUEST_TASK,
                new JsonObject().put("link", link));

            vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescriptor));
            event.response().end("Tas submitted");
          } else {
            event.fail(new IllegalArgumentException("Attribute link not specified"));
          }
        });

    httpServer.requestHandler(router::accept).listen(8444);
  }
}
