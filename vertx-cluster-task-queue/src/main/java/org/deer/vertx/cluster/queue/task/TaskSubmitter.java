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

import io.vertx.core.json.JsonObject;
import org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority;

public interface TaskSubmitter {

  default TaskDescription createTaskDescriptor(final String taskName, final JsonObject taskParams) {
    return TaskDescription.create(taskName, taskParams);
  }

  default TaskDescription createTaskDescriptor(final String taskName, final JsonObject taskParams,
      final TaskPriority priority) {
    return TaskDescription.create(taskName, taskParams, priority);
  }
}
