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

package org.deer.vertx.mma.rankings.task.download.and.save.link.scenario;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.deer.vertx.cluster.common.mongo.MongoClientUser;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.task.base.FighterProfileParseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FighterProfileLinkSaveTask extends FighterProfileParseTask
    implements MongoClientUser, TaskSubmitter {

  private static final Logger LOG = LoggerFactory.getLogger(FighterProfileLinkSaveTask.class);

  public static final String FIGHTER_PROFILE_LINK_AND_SAVE_TASK = "fighter-profile-link-and-save-task";

  private final MongoClient client;

  public FighterProfileLinkSaveTask(QueuedTask task, MongoClient client) {
    super(task, FIGHTER_PROFILE_LINK_AND_SAVE_TASK);
    this.client = client;
  }

  @Override
  public void onPageParsed(final JsonArray fighterProfiles, final Future<Void> resultHandler) {

    //saves currently processed link
    final String originalLink = task.getDescription().parseParams()
        .getString("original-link");
    final Future<String> saveFuture = Future.future();
    client.save("fighter-link", new JsonObject().put("link", originalLink), saveFuture);

    fighterProfiles.stream()
        .map(JsonObject.class::cast)
        .map(entries -> entries.getString("oponent-link"))
        .forEach(link -> vertx.eventBus().send("process-fighter-link", link));

    saveFuture.setHandler(event -> {
      if (event.failed()) {
        resultHandler.fail(event.cause());
      } else {
        resultHandler.complete();
      }
    });
  }
}
