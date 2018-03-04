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

package org.deer.vertx.mma.rankings.task.base;

import static com.hazelcast.util.Preconditions.checkState;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.stream.Collectors;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskStatsUpdater;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.task.impl.ProcessedLinksRegistryAccessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse fighter profile from www.mixedmartialarts.com
 */
public abstract class FighterProfileParseTask extends AbstractTaskExecutor<JsonArray> implements
    ProcessedLinksRegistryAccessor, TaskSubmitter, TaskStatsUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(FighterProfileParseTask.class);

  private static final String TO_BE_DETERMINED = "TBD";

  protected final QueuedTask task;

  public FighterProfileParseTask(final QueuedTask task, final String type) {
    super(type);
    checkState(task.getDescription().parseParams().containsKey("page-content"),
        "Page content not specified");
    checkState(task.getDescription().parseParams().containsKey("original-link"),
        "Original link not specified");
    this.task = task;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    reportTaskStarted(vertx, task);
    final String deploymentId = vertx.getOrCreateContext().deploymentID();
    this.perform(taskDoneEvent -> {
      if (taskDoneEvent.failed()) {
        reportTaskFailed(vertx, task, taskDoneEvent.cause().getMessage());
        return;
      }
      onPageParsed(taskDoneEvent.result(), startFuture);
      reportTaskFinished(vertx, task);
      vertx.undeploy(deploymentId);
    });
  }

  @Override
  public void perform(Handler<AsyncResult<JsonArray>> handler) {
    LOG.info("Parsing {}", task.getDescription().parseParams().getString("original-link"));
    final Document document = Jsoup
        .parse(task.getDescription().parseParams().getString("page-content"));

    final Elements profesionalRecordTable = document.select(profesionalMmaRecord())
        .select(fighterRecord())
        .get(0)// get first table - its the pro record
        .select(tableBody())
        .select(tableRow());

    final JsonArray processed = new JsonArray(profesionalRecordTable.stream()
        .filter(FighterProfileParseTask::noFutureFights)
        .map(element -> {
          final Elements visibleElements = element.select(visibleFightRecordElements());
          final Elements oponentElement = visibleElements.select(childNr(4));
          final String firstName = oponentElement.text().split(" ")[0];
          final String surName = oponentElement.text().replace(firstName, "").trim();

          return new JsonObject()
              .put("date", visibleElements.select(childNr(2))
                  .select(visibleFightRecordElements()).text())
              .put("fight-end", visibleElements.select(childNr(3)).text())
              .put("oponent-link", extactLink(oponentElement))
              .put("oponent-first-name", firstName)
              .put("oponent-last-name", surName)
              .put("event", visibleElements.select(childNr(5)).text())
              .put("fight-end-type", visibleElements.select(childNr(6)).text())
              .put("stopage-round", visibleElements.select(childNr(7)).text())
              .put("stopage-time", visibleElements.select(childNr(8)).text());
        }).collect(Collectors.toList()));
    handler.handle(Future.succeededFuture(processed));
  }

  public abstract void onPageParsed(final JsonArray fighterProfiles,
      final Future<Void> resultHandler);

  private static String profesionalMmaRecord() {
    return ".prof-mma-record";
  }

  private static String fighterRecord() {
    return ".fighter-record";
  }

  private static String tableBody() {
    return "tbody";
  }

  private static String tableRow() {
    return "tr";
  }

  private static String extactLink(Elements element) {
    return element.select("a").attr("href");
  }

  private static String childNr(final int nr) {
    return ":nth-child(" + nr + ")";
  }

  private static String visibleFightRecordElements() {
    return ".footable-visible";
  }

  private static boolean noFutureFights(final Element element) {
    return !element.select(visibleFightRecordElements() + childNr(3)).text()
        .equals(TO_BE_DETERMINED);
  }
}
