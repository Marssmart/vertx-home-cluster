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

package org.deer.vertx.mma.rankings.task.scenario.generate.fighters.from.links;

import static org.deer.vertx.mma.rankings.task.scenario.generate.fighters.from.links.ParserUtils.nameFromLink;
import static org.deer.vertx.mma.rankings.task.scenario.generate.fighters.from.links.ParserUtils.surnameFromLink;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskStatsUpdater;
import org.deer.vertx.mma.rankings.dto.Fighter;

public class GenerateFighterFromLinkTask extends AbstractTaskExecutor<String>
    implements TaskStatsUpdater {

  public static final String GENERATE_FIGHTER_FROM_LINK_TASK = "generate-fighter-from-link-task";

  private final QueuedTask queuedTask;
  private final MongoClient mongoClient;

  public GenerateFighterFromLinkTask(final QueuedTask queuedTask,
      final MongoClient mongoClient) {
    super(GENERATE_FIGHTER_FROM_LINK_TASK);
    this.queuedTask = queuedTask;
    this.mongoClient = mongoClient;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    reportTaskStarted(vertx, queuedTask);
    this.perform(stringAsyncResult -> {
      if (stringAsyncResult.succeeded()) {
        startFuture.complete();
        reportTaskFinished(vertx, queuedTask);
      } else {
        startFuture.fail(stringAsyncResult.cause());
        reportTaskFailed(vertx, queuedTask, stringAsyncResult.cause().getMessage());
      }
    });
  }

  @Override
  public void perform(Handler<AsyncResult<String>> handler) {
    final String link = queuedTask.getDescription().parseParams().getString("link");
    final Long ref = queuedTask.getDescription().parseParams().getLong("ref");

    final Fighter fighter = new Fighter(ref, nameFromLink(link), surnameFromLink(link), link);

    mongoClient.save("fighter", JsonObject.mapFrom(fighter), handler);
  }

}
