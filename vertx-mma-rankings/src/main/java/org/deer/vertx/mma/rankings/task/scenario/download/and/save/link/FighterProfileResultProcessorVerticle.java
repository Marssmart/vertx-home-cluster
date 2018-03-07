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

package org.deer.vertx.mma.rankings.task.scenario.download.and.save.link;

import static org.deer.vertx.mma.rankings.task.scenario.download.and.save.link.PageRequestWithLinkParseAndSaveTask.PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FighterProfileResultProcessorVerticle extends AbstractVerticle
    implements TaskSubmitter {

  private static final Logger LOG = LoggerFactory.getLogger(TaskSubmitter.class);

  private final Set<String> processedLinks;
  private final Lock processLinksLock;

  public FighterProfileResultProcessorVerticle() {
    processedLinks = new LinkedHashSet<>();
    processLinksLock = new ReentrantLock();
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    vertx.eventBus().consumer("process-fighter-link")
        .handler(message -> {
          final String link = String.class.cast(message.body());

          processLinksLock.lock();
          if (!processedLinks.contains(link)) {
            LOG.info("New unique link found: {}", link);
            final TaskDescription description = createTaskDescriptor(
                PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK,
                new JsonObject().put("link", link), TaskPriority.LOW);

            vertx.eventBus().send("task-submit", JsonObject.mapFrom(description));
            processedLinks.add(link);
          }
          processLinksLock.unlock();
        });
  }
}
