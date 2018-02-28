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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import java.util.Comparator;

public class TaskDescription {

  public static final Comparator<TaskDescription> HIGH_TO_LOW_PRIORITY_ORDER = new Comparator<TaskDescription>() {
    @Override
    public int compare(TaskDescription first, TaskDescription second) {
      return second.priority.value - first.priority.value;
    }
  };

  @JsonProperty
  private String name;

  @JsonProperty
  private String params;

  @JsonProperty
  private TaskPriority priority;

  private TaskDescription() {
    this(null, null, null);
  }

  private TaskDescription(String name, JsonObject params, TaskPriority priority) {
    this.name = name;
    this.params = params != null ? params.encode() : null;
    this.priority = priority;
  }

  public static TaskDescription create(String name, JsonObject params) {
    return new TaskDescription(name, params, TaskPriority.LOW);
  }

  public static TaskDescription create(String name, JsonObject params, TaskPriority priority) {
    return new TaskDescription(name, params, priority);
  }

  public static TaskDescription empty() {
    return new TaskDescription();
  }

  @JsonIgnore
  public boolean isEmpty() {
    return name == null;
  }

  @JsonIgnore
  public JsonObject parseParams() {
    return params != null ? new JsonObject(params) : null;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public void setPriority(TaskPriority priority) {
    this.priority = priority;
  }

  public enum TaskPriority {
    HIGH(2),
    MEDIUM(1),
    LOW(0);

    private final int value;

    TaskPriority(int value) {
      this.value = value;
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TaskDescription that = (TaskDescription) o;

    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    return params != null ? params.equals(that.params) : that.params == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (params != null ? params.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TaskDescription{" +
        "name='" + name + '\'' +
        ", params='" + params + '\'' +
        ", priority=" + priority +
        '}';
  }
}
