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

import io.vertx.core.AbstractVerticle;

public abstract class AbstractTaskExecutor<ResultType> extends AbstractVerticle
    implements TaskExecutor<ResultType> {

  private final String taskType;

  protected AbstractTaskExecutor(String taskType) {
    this.taskType = taskType;
  }

  public String getTaskType() {
    return taskType;
  }
}
