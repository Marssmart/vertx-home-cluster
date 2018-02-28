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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.factory.TaskExecutorFactory;
import org.deer.vertx.mma.rankings.task.factory.impl.TaskExecutorFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskPerformerVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(TaskPerformerVerticle.class);

  private final TaskExecutorFactory executorFactory;

  public TaskPerformerVerticle() {
    executorFactory = new TaskExecutorFactoryImpl();
  }

  @Override
  public void start() throws Exception {
    vertx.setPeriodic(3000, event -> {
      final Future<Message<Object>> taskRetrievalFuture = Future.future();
      vertx.eventBus().send("task-get", null, taskRetrievalFuture);

      final Future<String> deployFuture = Future.future();
      taskRetrievalFuture.setHandler(taskRetrievalEvent -> {
        if (taskRetrievalEvent.succeeded()) {
          final TaskDescription taskDescription = JsonObject
              .mapFrom(taskRetrievalEvent.result().body())
              .mapTo(TaskDescription.class);

          if (taskDescription.isEmpty()) {
            LOG.trace("No tasks in queue");
            return;
          }

          LOG.info("Starting {}", taskDescription.getName());

          final AbstractTaskExecutor executor = executorFactory.createExecutor(taskDescription);
          vertx.deployVerticle(executor, new DeploymentOptions().setWorker(true), deployFuture);
        } else {
          LOG.trace("Cannot retrieve task", taskRetrievalEvent.cause());
        }
      });

      deployFuture.setHandler(deployEvent -> {
        if (deployEvent.failed()) {
          LOG.error("Task failed", deployEvent.cause());
        }
      });
    });
  }
}
