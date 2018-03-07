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

import static org.deer.vertx.cluster.queue.task.QueuedTaskState.FAILED;
import static org.deer.vertx.cluster.queue.task.QueuedTaskState.FINISHED;
import static org.deer.vertx.cluster.queue.task.QueuedTaskState.RETRIEVED;
import static org.deer.vertx.cluster.queue.task.QueuedTaskState.STARTED;
import static org.deer.vertx.cluster.queue.task.QueuedTaskState.SUBMITED;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.LOW;
import static org.deer.vertx.mma.rankings.task.scenario.download.and.save.link.PageRequestWithLinkParseAndSaveTask.PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK;
import static org.deer.vertx.mma.rankings.task.scenario.generate.fighters.from.links.GenerateFighterFromLinkTask.GENERATE_FIGHTER_FROM_LINK_TASK;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import java.util.concurrent.atomic.AtomicLong;
import org.deer.vertx.cluster.common.mongo.MongoClientUser;
import org.deer.vertx.cluster.queue.task.QueuedTaskState;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.task.scenario.download.and.save.fighter.profiles.DownloadFighterProfileTask;


public class ScrapeHttpServerVerticle extends AbstractVerticle implements TaskSubmitter,
    MongoClientUser {

  @Override
  public void start() throws Exception {
    final HttpServer httpServer = vertx.createHttpServer();
    final Router router = Router.router(vertx);

    router.route("/api/v1/mma/scrape-fighter-profile/stats")
        .handler(event -> connectToMongo(vertx)
            .setHandler(connectionEvent -> {
              if (connectionEvent.succeeded()) {
                final MongoClient client = connectionEvent.result();

                final Future<Long> startedCount = Future.future();
                final Future<Long> retrievedCount = Future.future();
                final Future<Long> failedCount = Future.future();
                final Future<Long> finishedCount = Future.future();
                final Future<Long> submittedCount = Future.future();

                client.count("task-stats", filterState(STARTED), startedCount);
                client.count("task-stats", filterState(RETRIEVED), retrievedCount);
                client.count("task-stats", filterState(FAILED), failedCount);
                client.count("task-stats", filterState(FINISHED), finishedCount);
                client.count("task-stats", filterState(SUBMITED), submittedCount);

                CompositeFuture
                    .all(startedCount, retrievedCount, failedCount, finishedCount, submittedCount)
                    .setHandler(allDoneEvent -> {
                      client.close();
                      if (allDoneEvent.succeeded()) {
                        final Long started = startedCount.result();
                        final Long retrieved = retrievedCount.result();
                        final Long failed = failedCount.result();
                        final Long finished = finishedCount.result();
                        final Long submitted = submittedCount.result();

                        event.response().end(new JsonObject()
                            .put("submitted", submitted)
                            .put("retrieved", retrieved)
                            .put("started", started)
                            .put("failed", failed)
                            .put("finished", finished)
                            .put("in-progress", finished - started)
                            .toBuffer());
                      } else {
                        event.fail(connectionEvent.cause());
                      }

                    });
              } else {
                event.fail(connectionEvent.cause());
              }
            }));

    router.route("/api/v1/mma/scrape-fighter-profile")
        .handler(event -> {
          final MultiMap params = event.request().params();

          final String link = params.get("link");
          if (link != null) {
            // creates context on entry point to chain of tasks
            final TaskDescription taskDescriptor = createTaskDescriptor(
                PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK,
                new JsonObject().put("link", link), LOW);

            vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescriptor));
            event.response().end("Tas submitted");
          } else {
            event.fail(new IllegalArgumentException("Attribute link not specified"));
          }
        });

    router.route("/api/v1/mma/generate-fighters-from-links")
        .handler(routingContext -> connectDedicatedToMongo(vertx)
            .setHandler(mongoClientAsyncResult -> {
              if (mongoClientAsyncResult.succeeded()) {
                final MongoClient client = mongoClientAsyncResult.result();

                final AtomicLong refGenerator = new AtomicLong(1);
                client.findBatch("fighter-link", new JsonObject())
                    .handler(entries -> {
                      final TaskDescription taskDescriptor = createTaskDescriptor(
                          GENERATE_FIGHTER_FROM_LINK_TASK,
                          new JsonObject().put("link", entries.getString("link"))
                              .put("ref", refGenerator.getAndIncrement()));

                      vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescriptor));
                    })
                    .endHandler(aVoid -> {
                      routingContext.response().end("All tasks submited");
                      client.close();
                    })
                    .exceptionHandler(routingContext::fail);
              } else {
                routingContext.fail(mongoClientAsyncResult.cause());
              }
            }));

    router.route("/api/v1/mma/download-fights")
        .handler(routingContext -> {
          connectDedicatedToMongo(vertx)
              .setHandler(mongoClientAsyncResult -> {
                if (mongoClientAsyncResult.succeeded()) {
                  final MongoClient client = mongoClientAsyncResult.result();

                  client.findBatchWithOptions("fighter", new JsonObject(),
                      new FindOptions().setFields(new JsonObject().put("profileLink", 1)
                          .put("ref", 1)))
                      .handler(entries -> {

                        final JsonObject entry = JsonObject.class.cast(entries);

                        final Long fighterRef = entry.getLong("ref");
                        final String profileLink = entry.getString("profileLink");

                        final TaskDescription taskDescriptor = createTaskDescriptor(
                            DownloadFighterProfileTask.DOWNLOAD_FIGHTER_PROFILE,
                            new JsonObject().put("link", profileLink)
                                .put("fighter-ref", fighterRef));

                        vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescriptor));
                      })
                      .exceptionHandler(routingContext::fail)
                      .endHandler(aVoid -> {
                        routingContext.response().end("All tasks submitted");
                        client.close();
                      });
                } else {
                  routingContext.fail(mongoClientAsyncResult.cause());
                }
              });
        });

    httpServer.requestHandler(router::accept).listen(8445);
  }

  private static JsonObject filterState(QueuedTaskState started) {
    return new JsonObject().put("state", started);
  }
}
