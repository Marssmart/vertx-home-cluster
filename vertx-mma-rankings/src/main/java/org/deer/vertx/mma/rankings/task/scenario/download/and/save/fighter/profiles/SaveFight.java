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

package org.deer.vertx.mma.rankings.task.scenario.download.and.save.fighter.profiles;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.mma.rankings.dto.Fight;

public class SaveFight extends AbstractTaskExecutor<Void> {

  public static final String SAVE_FIGHT_TASK = "save-fight-task";
  private final MongoClient client;
  private final QueuedTask task;

  public SaveFight(MongoClient client, QueuedTask task) {
    super(SAVE_FIGHT_TASK);
    this.client = client;
    this.task = task;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    this.perform(startFuture);
  }

  @Override
  public void perform(Handler<AsyncResult<Void>> handler) {
    final JsonObject params = task.getDescription().parseParams();
    final long fighterRef = params.getLong("fighter-ref");
    final JsonObject data = params.getJsonObject("data");

    //mapping to dto to verify structure is intact
    final Fight fight = data.put("fighter-ref", fighterRef).mapTo(Fight.class);

    client.save("fight", JsonObject.mapFrom(fight), stringAsyncResult -> {
      if (stringAsyncResult.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(stringAsyncResult.cause()));
      }
    });
  }
}
