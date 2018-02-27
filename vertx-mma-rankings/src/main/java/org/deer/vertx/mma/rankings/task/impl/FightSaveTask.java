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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FightSaveTask extends AbstractTaskExecutor<Void> {

  private static final Logger LOG = LoggerFactory.getLogger(FightSaveTask.class);

  public static final String FIGHT_SAVE_TASK = "fight-save-task";

  private final JsonObject data;
  private final String originalLink;
  private final Integer perFighterIndex;

  public FightSaveTask(final JsonObject params) {
    super(FIGHT_SAVE_TASK);
    perFighterIndex = params.getInteger("per-fighter-index");
    data = params.getJsonObject("data");
    originalLink = params.getString("original-link");
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    this.perform(startFuture);
  }

  @Override
  public void perform(Handler<AsyncResult<Void>> handler) {
    //TODO change this to mongo DB save

    final String fileName = filenameFromLink(originalLink, perFighterIndex);
    final String folder = "C://scrape";
    final String path = folder + "//" + fileName;

    if (!vertx.fileSystem().existsBlocking(path)) {
      vertx.fileSystem()
          .createFileBlocking(path)
          .writeFileBlocking(path, data.toBuffer());
    } else {
      LOG.error("File {} already exist", path);
    }
  }

  private static String filenameFromLink(String link, int index) {
    final String base = "/fighter/";
    return (link.substring(link.indexOf(base) + base.length(),
        link.indexOf(":", link.indexOf(base))) + "_" + index + ".json").trim();
  }
}
