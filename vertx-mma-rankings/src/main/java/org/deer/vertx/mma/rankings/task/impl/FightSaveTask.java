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

package org.deer.vertx.mma.rankings.task.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.stream.Collectors;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FightSaveTask extends AbstractTaskExecutor<Void> implements MongoClientUser {

  private static final Logger LOG = LoggerFactory.getLogger(FightSaveTask.class);

  public static final String FIGHT_SAVE_TASK = "fight-save-task";

  private final JsonArray data;
  private final String originalLink;

  public FightSaveTask(final JsonObject params) {
    super(FIGHT_SAVE_TASK);
    data = params.getJsonArray("data");
    originalLink = params.getString("original-link");
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    this.perform(startFuture);
  }

  @Override
  public void perform(Handler<AsyncResult<Void>> handler) {
    connectToMongo(vertx)
        .setHandler(connectionEvent -> {
          if (connectionEvent.succeeded()) {
            final MongoClient client = connectionEvent.result();

            CompositeFuture.all(data.stream()
                .map(JsonObject.class::cast)
                .map(entries -> entries.put("fighter-full-name", nameFromLink(originalLink)))
                .map(singleFight -> {
                  final Future<String> saveFuture = Future.future();
                  client.save("fight", singleFight, saveFuture);
                  return saveFuture;
                }).collect(Collectors.toList()))
                .setHandler(event -> {
                  if (event.succeeded()) {
                    LOG.info("{} fights saved for link {}", event.result().list().size(),
                        originalLink);
                  } else {
                    LOG.error("Error saving fights", event.cause());
                  }
                  client.close();
                });
          }
        });
  }

  private static String nameFromLink(String link) {
    final String base = "/fighter/";
    return (link.substring(link.indexOf(base) + base.length(),
        link.indexOf(":", link.indexOf(base))))
        .replace("-"," ").trim();
  }
}
