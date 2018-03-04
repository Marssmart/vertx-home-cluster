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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FailedTask {

  private final String message;
  private final QueuedTask task;

  @JsonCreator
  public FailedTask(@JsonProperty("message") final String message,
      @JsonProperty("task") final QueuedTask task) {
    this.message = message;
    this.task = task;
  }

  public String getMessage() {
    return message;
  }

  public QueuedTask getTask() {
    return task;
  }
}
