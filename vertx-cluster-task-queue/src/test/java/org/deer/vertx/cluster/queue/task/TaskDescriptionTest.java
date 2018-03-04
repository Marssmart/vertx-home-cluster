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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.json.JsonObject;
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
    assertEquals(taskDescription, deserialized);
  }
}