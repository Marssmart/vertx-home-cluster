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

import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.HIGH;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.LOW;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.MEDIUM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaskDescriptionTest {

  @Test
  public void testSerialize() {
    final TaskDescription taskDescription = TaskDescription
        .create("name", new JsonObject().put("link", "link-data"));

    final JsonObject data = JsonObject.mapFrom(taskDescription);
    final String stringData = data.encode();

    final JsonObject parsed = new JsonObject(stringData);
    final TaskDescription deserialized = parsed.mapTo(TaskDescription.class);
    Assertions.assertEquals(taskDescription, deserialized);
  }

  @Test
  public void testOrderHightToLow() {
    final TaskDescription low = TaskDescription.create("low", null, LOW);
    final TaskDescription medium = TaskDescription.create("medium", null, MEDIUM);
    final TaskDescription high = TaskDescription.create("high", null, HIGH);

    final List<TaskDescription> ordered = Stream.of(low, high, medium)
        .sorted(TaskDescription.HIGH_TO_LOW_PRIORITY_ORDER)
        .collect(Collectors.toList());

    assertThat(ordered, contains(is(high), is(medium), is(low)));
  }
}