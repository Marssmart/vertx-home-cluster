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

package org.deer.vertx.cluster.queue.task.factory;

import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskDescription;

public interface TaskExecutorFactory {

  AbstractTaskExecutor<?> createExecutor(final QueuedTask queuedTask);
}
