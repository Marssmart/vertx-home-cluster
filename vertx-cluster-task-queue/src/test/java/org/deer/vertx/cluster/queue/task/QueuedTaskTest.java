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

import static org.deer.vertx.cluster.queue.task.QueuedTask.HIGH_TO_LOW_PRIORITY_ORDER;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.HIGH;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.LOW;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.MEDIUM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vertx.core.json.JsonObject;
import java.util.PriorityQueue;
import org.junit.jupiter.api.Test;

public class QueuedTaskTest {

  @Test
  public void testSerialize() {
    final QueuedTask queuedTask = new QueuedTask(2L,
        TaskDescription.create("tst-name", new JsonObject()));

    final QueuedTask deserialized = new JsonObject(JsonObject.mapFrom(queuedTask).encode())
        .mapTo(QueuedTask.class);

    assertEquals(queuedTask, deserialized);
  }

  @Test
  public void testOrder() {
    final PriorityQueue<QueuedTask> queue = new PriorityQueue<>(5,
        HIGH_TO_LOW_PRIORITY_ORDER);

    queue.add(new QueuedTask(1L, TaskDescription.create("one", null, LOW)));
    queue.add(new QueuedTask(1L, TaskDescription.create("two", null, MEDIUM)));
    queue.add(new QueuedTask(1L, TaskDescription.create("three", null, HIGH)));

    assertTrue(queue.poll().getDescription().getPriority() == HIGH);
    assertTrue(queue.poll().getDescription().getPriority() == MEDIUM);
    assertTrue(queue.poll().getDescription().getPriority() == LOW);
  }

}