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

package org.deer.vertx.cluster.queue.task;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

public interface TaskStatsUpdater {

  default void reportTaskStarted(final Vertx vertx, final QueuedTask task) {
    vertx.eventBus().send("task-started", JsonObject.mapFrom(task));
  }


  default void reportTaskFailed(final Vertx vertx, final QueuedTask task, final String message) {
    vertx.eventBus().send("task-failed", JsonObject.mapFrom(new FailedTask(message, task)));
  }


  default void reportTaskFinished(final Vertx vertx, final QueuedTask task) {
    vertx.eventBus().send("task-finished", JsonObject.mapFrom(task));
  }
}
