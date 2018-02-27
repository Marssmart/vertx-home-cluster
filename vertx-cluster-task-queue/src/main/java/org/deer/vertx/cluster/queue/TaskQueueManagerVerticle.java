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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.LinkedList;
import java.util.Queue;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.queue.deploy.DeployManager;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TaskQueueManagerVerticle extends AbstractVerticle implements DeployManager {

  private static final Logger LOG = LoggerFactory.getLogger(TaskQueueManagerVerticle.class);

  static final String DEPLOY_MARKER = "task-queue-deployed";
  static final String DEPLOY_LOCK = "task-queue-deploy-lock";

  private final Queue<TaskDescription> taskQueue;

  public TaskQueueManagerVerticle() {
    this.taskQueue = new LinkedList<>();
  }

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      INSTANCE.deployClusterSingleton(vertx,
          TaskQueueManagerVerticle.DEPLOY_MARKER,
          TaskQueueManagerVerticle.DEPLOY_LOCK,
          TaskQueueManagerVerticle.class.getName(),
          LOG);
    });
  }


  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("task-submit")
        .handler(event -> {
          taskQueue.add(JsonObject.class.cast(event.body()).mapTo(TaskDescription.class));
        });

    vertx.eventBus().consumer("task-get")
        .handler(event -> {
          if (taskQueue.isEmpty()) {
            event.reply(JsonObject.mapFrom(TaskDescription.empty()));
          } else {
            event.reply(JsonObject.mapFrom(taskQueue.poll()));
          }
        });
  }
}
