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

package org.deer.vertx.mma.rankings.task.scenario.download.and.save.fighter.profiles;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.vertx.core.json.JsonObject;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.task.base.PageRequestTask;

public class DownloadFighterProfileTask extends PageRequestTask implements TaskSubmitter {

  public static final String DOWNLOAD_FIGHTER_PROFILE = "download-fighter-profile";

  public DownloadFighterProfileTask(QueuedTask task) {
    super(task, DOWNLOAD_FIGHTER_PROFILE);
  }

  @Override
  public void onPageRequestFinished(HtmlPage page) {
    final long fighterRef = task.getDescription().parseParams().getLong("fighter-ref");

    final TaskDescription taskDescriptor = createTaskDescriptor(
        ParseFighterProfile.PARSE_FIGHTER_PROFILE,
        createPageParseParams(page).put("fighter-ref", fighterRef), TaskPriority.MEDIUM);

    vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescriptor));
  }
}
