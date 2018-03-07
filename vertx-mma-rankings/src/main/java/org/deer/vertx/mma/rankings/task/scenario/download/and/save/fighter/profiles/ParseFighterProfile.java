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

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.task.base.FighterProfileParseTask;

public class ParseFighterProfile extends FighterProfileParseTask
    implements TaskSubmitter {

  public static final String PARSE_FIGHTER_PROFILE = "parse-fighter-profile";

  public ParseFighterProfile(QueuedTask task) {
    super(task, PARSE_FIGHTER_PROFILE);
  }

  @Override
  public void onPageParsed(JsonArray fighterProfiles, Future<Void> resultHandler) {
    final long fighterRef = task.getDescription().parseParams().getLong("fighter-ref");

    fighterProfiles.forEach(o -> {
      final JsonObject data = JsonObject.class.cast(o);

      final TaskDescription taskDescriptor = createTaskDescriptor(SaveFight.SAVE_FIGHT_TASK,
          new JsonObject().put("fighter-ref", fighterRef).put("data", data), TaskPriority.HIGH);

      vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescriptor));
    });

    resultHandler.complete();
  }
}
