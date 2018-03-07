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
import org.deer.vertx.cluster.common.mongo.MongoClientUser;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.factory.TaskExecutorFactory;
import org.deer.vertx.mma.rankings.task.factory.impl.TaskExecutorFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskPerformerVerticle extends AbstractVerticle implements MongoClientUser {

  private static final Logger LOG = LoggerFactory.getLogger(TaskPerformerVerticle.class);

  private TaskExecutorFactory executorFactory;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    connectToMongo(vertx)
        .setHandler(clientResult -> {
          if (clientResult.failed()) {
            startFuture.fail(clientResult.cause());
          }

          executorFactory = new TaskExecutorFactoryImpl(clientResult.result());
          retrieveAndStartTask();
          startFuture.complete();
        });
  }

  private void retrieveAndStartTask() {
    final Future<Message<Object>> taskRetrievalFuture = Future.future();
    vertx.eventBus().send("task-get", null, taskRetrievalFuture);

    final Future<String> deployFuture = Future.future();
    taskRetrievalFuture.setHandler(taskRetrievalEvent -> {
      if (taskRetrievalEvent.succeeded()) {
        if (taskRetrievalEvent.result().body() == null) {
          LOG.trace("No tasks in queue");
          //no task retrieved, wait 50 millis and try again
          vertx.setTimer(50, aLong -> retrieveAndStartTask());
          return;
        }

        final QueuedTask queuedTask = JsonObject
            .mapFrom(taskRetrievalEvent.result().body())
            .mapTo(QueuedTask.class);

        LOG.info("Starting {}", queuedTask.getDescription().getName());

        final AbstractTaskExecutor executor = executorFactory.createExecutor(queuedTask);
        vertx.deployVerticle(executor, new DeploymentOptions().setWorker(true),
            deployFuture);
      } else {
        LOG.trace("Cannot retrieve task", taskRetrievalEvent.cause());
        //failed to retrieve task, wait 50 millis and try again
        vertx.setTimer(50, aLong -> retrieveAndStartTask());
      }
    });

    deployFuture.setHandler(deployEvent -> {
      if (deployEvent.failed()) {
        LOG.error("Task failed", deployEvent.cause());
      }
      //start another
      retrieveAndStartTask();
    });
  }
}
