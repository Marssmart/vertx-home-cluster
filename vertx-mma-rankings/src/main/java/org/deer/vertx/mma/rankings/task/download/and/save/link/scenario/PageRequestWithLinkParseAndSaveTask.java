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

package org.deer.vertx.mma.rankings.task.download.and.save.link.scenario;

import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.HIGH;
import static org.deer.vertx.mma.rankings.task.download.and.save.link.scenario.FighterProfileLinkSaveTask.FIGHTER_PROFILE_LINK_AND_SAVE_TASK;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.vertx.core.json.JsonObject;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.mma.rankings.task.base.PageRequestTask;

public class PageRequestWithLinkParseAndSaveTask extends PageRequestTask {

  public static final String PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK = "page-request-with-link-parse-and-save";

  public PageRequestWithLinkParseAndSaveTask(QueuedTask task) {
    super(task, PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK);
  }

  @Override
  public void onPageRequestFinished(HtmlPage page) {
    final TaskDescription taskDescription = createTaskDescriptor(
        FIGHTER_PROFILE_LINK_AND_SAVE_TASK, createPageParseParams(page), HIGH);

    vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescription));
  }
}
