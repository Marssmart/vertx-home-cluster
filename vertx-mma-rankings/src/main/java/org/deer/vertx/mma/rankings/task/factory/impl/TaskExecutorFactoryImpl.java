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

package org.deer.vertx.mma.rankings.task.factory.impl;


import io.vertx.ext.mongo.MongoClient;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.factory.TaskExecutorFactory;
import org.deer.vertx.mma.rankings.task.download.and.save.link.scenario.FighterProfileLinkSaveTask;
import org.deer.vertx.mma.rankings.task.download.and.save.link.scenario.PageRequestWithLinkParseAndSaveTask;

public class TaskExecutorFactoryImpl implements TaskExecutorFactory {

  private final MongoClient mongoClient;

  public TaskExecutorFactoryImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public AbstractTaskExecutor<?> createExecutor(final QueuedTask queuedTask) {

    final TaskDescription taskDescription = queuedTask.getDescription();
    switch (taskDescription.getName()) {
      case FighterProfileLinkSaveTask.FIGHTER_PROFILE_LINK_AND_SAVE_TASK: {
        return new FighterProfileLinkSaveTask(queuedTask, mongoClient);
      }
      case PageRequestWithLinkParseAndSaveTask.PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK: {
        return new PageRequestWithLinkParseAndSaveTask(queuedTask);
      }
      default:
        throw new IllegalStateException("No executor for type " + taskDescription.getName());
    }
  }
}
