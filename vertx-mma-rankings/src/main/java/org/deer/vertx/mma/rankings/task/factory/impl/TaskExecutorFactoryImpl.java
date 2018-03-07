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

package org.deer.vertx.mma.rankings.task.factory.impl;


import io.vertx.ext.mongo.MongoClient;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.factory.TaskExecutorFactory;
import org.deer.vertx.mma.rankings.task.scenario.download.and.save.fighter.profiles.DownloadFighterProfileTask;
import org.deer.vertx.mma.rankings.task.scenario.download.and.save.fighter.profiles.ParseFighterProfile;
import org.deer.vertx.mma.rankings.task.scenario.download.and.save.fighter.profiles.SaveFight;
import org.deer.vertx.mma.rankings.task.scenario.download.and.save.link.FighterProfileLinkSaveTask;
import org.deer.vertx.mma.rankings.task.scenario.download.and.save.link.PageRequestWithLinkParseAndSaveTask;
import org.deer.vertx.mma.rankings.task.scenario.generate.fighters.from.links.GenerateFighterFromLinkTask;

public class TaskExecutorFactoryImpl implements TaskExecutorFactory {

  private final MongoClient mongoClient;

  public TaskExecutorFactoryImpl(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  @Override
  public AbstractTaskExecutor<?> createExecutor(final QueuedTask queuedTask) {

    final TaskDescription taskDescription = queuedTask.getDescription();
    switch (taskDescription.getName()) {
      case FighterProfileLinkSaveTask.FIGHTER_PROFILE_LINK_AND_SAVE_TASK: {
        return new FighterProfileLinkSaveTask(queuedTask, mongoClient);
      }
      case PageRequestWithLinkParseAndSaveTask.PAGE_REQUEST_WITH_LINK_PARSE_AND_SAVE_TASK: {
        return new PageRequestWithLinkParseAndSaveTask(queuedTask);
      }
      case GenerateFighterFromLinkTask.GENERATE_FIGHTER_FROM_LINK_TASK: {
        return new GenerateFighterFromLinkTask(queuedTask, mongoClient);
      }
      case DownloadFighterProfileTask.DOWNLOAD_FIGHTER_PROFILE: {
        return new DownloadFighterProfileTask(queuedTask);
      }
      case ParseFighterProfile.PARSE_FIGHTER_PROFILE: {
        return new ParseFighterProfile(queuedTask);
      }
      case SaveFight.SAVE_FIGHT_TASK: {
        return new SaveFight(mongoClient, queuedTask);
      }
      default:
        throw new IllegalStateException("No executor for type " + taskDescription.getName());
    }
  }
}
