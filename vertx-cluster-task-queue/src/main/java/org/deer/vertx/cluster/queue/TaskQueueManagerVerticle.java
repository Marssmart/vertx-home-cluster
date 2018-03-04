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

package org.deer.vertx.cluster.queue;

import static org.deer.vertx.cluster.queue.task.QueuedTask.HIGH_TO_LOW_PRIORITY_ORDER;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.common.mongo.MongoClientUser;
import org.deer.vertx.cluster.queue.deploy.DeployManager;
import org.deer.vertx.cluster.queue.task.FailedTask;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.QueuedTaskState;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TaskQueueManagerVerticle extends AbstractVerticle implements DeployManager,
    MongoClientUser {

  private static final Logger LOG = LoggerFactory.getLogger(TaskQueueManagerVerticle.class);

  private static final String DEPLOY_MARKER = "task-queue-deployed";
  private static final String DEPLOY_LOCK = "task-queue-deploy-lock";

  private final PriorityQueue<QueuedTask> taskQueue;
  private final AtomicLong taskIdCounter;

  public TaskQueueManagerVerticle() {
    this.taskQueue = new PriorityQueue<>(500, HIGH_TO_LOW_PRIORITY_ORDER);
    taskIdCounter = new AtomicLong(0L);
  }

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      INSTANCE.deployClusterSingleton(vertx,
          TaskQueueManagerVerticle.DEPLOY_MARKER,
          TaskQueueManagerVerticle.DEPLOY_LOCK,
          TaskQueueManagerVerticle.class.getName());
    });
  }


  @Override
  public void start() throws Exception {

    final Future<MongoClient> mongoClientFuture = connectDedicatedToMongo(vertx);

    mongoClientFuture.setHandler(connectionEvent -> {

      final MongoClient mongoClient = mongoClientFuture.result();

      vertx.eventBus().consumer("task-submit")
          .handler(event -> {
            final TaskDescription taskDescription = JsonObject.class.cast(event.body())
                .mapTo(TaskDescription.class);
            final QueuedTask queuedTask = new QueuedTask(taskIdCounter.getAndIncrement(),
                taskDescription);
            taskQueue.add(queuedTask);

            //updates statistics async
            updateStatsAsync(mongoClient, QueuedTaskState.SUBMITED, queuedTask);
          });

      vertx.eventBus().consumer("task-get")
          .handler(event -> {
            if (taskQueue.isEmpty()) {
              event.reply(null);
            } else {
              final QueuedTask queuedTask = taskQueue.poll();
              event.reply(JsonObject.mapFrom(queuedTask));
              //updates statistics async
              updateStatsAsync(mongoClient, QueuedTaskState.RETRIEVED, queuedTask);
            }
          });

      vertx.eventBus().consumer("task-started")
          .handler(event -> {
            final QueuedTask queuedTask = JsonObject.class.cast(event.body())
                .mapTo(QueuedTask.class);
            //updates statistics async
            updateStatsAsync(mongoClient, QueuedTaskState.STARTED, queuedTask);
          });

      vertx.eventBus().consumer("task-failed")
          .handler(event -> {
            final FailedTask failedTask = JsonObject.class.cast(event.body())
                .mapTo(FailedTask.class);
            //updates statistics async
            updateStatsAsync(mongoClient, QueuedTaskState.FAILED, failedTask);
          });

      vertx.eventBus().consumer("task-finished")
          .handler(event -> {
            final QueuedTask queuedTask = JsonObject.class.cast(event.body())
                .mapTo(QueuedTask.class);
            //updates statistics async
            updateStatsAsync(mongoClient, QueuedTaskState.FINISHED, queuedTask);
          });
    });
  }

  private void updateStatsAsync(final MongoClient client,
      final QueuedTaskState state,
      final QueuedTask task) {
    client.save("task-stats", createTaskStats(state, task), saveEvent -> {
      if (saveEvent.succeeded()) {
        LOG.trace("Statistics updated for task {}", task.getId());
      } else {
        LOG.error("Unable to update statistics {}", task.getId());
      }
    });
  }

  private void updateStatsAsync(final MongoClient client,
      final QueuedTaskState state,
      final FailedTask failedTask) {
    client.save("task-stats", createTaskStats(state, failedTask), saveEvent -> {
      if (saveEvent.succeeded()) {
        LOG.trace("Statistics updated for task {}", failedTask.getTask().getId());
      } else {
        LOG.error("Unable to update statistics {}", failedTask.getTask().getId());
      }
    });
  }

  private static JsonObject createTaskStats(final QueuedTaskState state,
      final QueuedTask task) {
    return new JsonObject()
        .put("state", state)
        .put("task-id", task.getId())
        .put("created-at", System.currentTimeMillis());
  }

  private static JsonObject createTaskStats(final QueuedTaskState state,
      final FailedTask failedTask) {
    return new JsonObject()
        .put("state", state)
        .put("task-id", failedTask.getTask().getId())
        .put("error-message", failedTask.getMessage())
        .put("created-at", System.currentTimeMillis());
  }
}
