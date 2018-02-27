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


import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.factory.TaskExecutorFactory;
import org.deer.vertx.mma.rankings.task.impl.FightSaveTask;
import org.deer.vertx.mma.rankings.task.impl.PageParseTask;
import org.deer.vertx.mma.rankings.task.impl.PageRequestTask;

public class TaskExecutorFactoryImpl implements TaskExecutorFactory {

  @Override
  public AbstractTaskExecutor<?> createExecutor(final TaskDescription taskDescription) {

    switch (taskDescription.getName()) {
      case PageRequestTask.PAGE_REQUEST_TASK: {
        return new PageRequestTask(taskDescription.parseParams());
      }
      case PageParseTask.PAGE_PARSE_TASK: {
        return new PageParseTask(taskDescription.parseParams());
      }
      case FightSaveTask.FIGHT_SAVE_TASK: {
        return new FightSaveTask(taskDescription.parseParams());
      }
      default:
        throw new IllegalStateException("No executor for type " + taskDescription.getName());
    }
  }
}
