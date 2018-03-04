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
import java.util.Comparator;

public class QueuedTask {

  public static final Comparator<QueuedTask> HIGH_TO_LOW_PRIORITY_ORDER = (first, second) ->
      second.description.getPriority().getValue() - first.description.getPriority().getValue();

  private final long id;
  private final TaskDescription description;

  @JsonCreator
  public QueuedTask(@JsonProperty("id") final long id,
      @JsonProperty("description") final TaskDescription description) {
    this.id = id;
    this.description = description;
  }

  public long getId() {
    return id;
  }

  public TaskDescription getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    QueuedTask that = (QueuedTask) o;

    if (id != that.id) {
      return false;
    }
    return description != null ? description.equals(that.description) : that.description == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}
