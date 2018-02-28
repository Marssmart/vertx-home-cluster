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

package org.deer.vertx.mma.rankings.task.impl;

import static org.deer.vertx.mma.rankings.task.impl.PageRequestTask.PAGE_REQUEST_TASK;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageParseTask extends AbstractTaskExecutor<JsonArray> implements
    ProcessedLinksRegistryAccessor {

  private static final Logger LOG = LoggerFactory.getLogger(PageParseTask.class);

  public static final String PAGE_PARSE_TASK = "page-parse-task";
  private static final String TO_BE_DETERMINED = "TBD";

  private final String content;
  private final String originalLink;

  public PageParseTask(final JsonObject params) {
    super(PAGE_PARSE_TASK);
    content = params.getString("page-content");
    originalLink = params.getString("original-link");
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    this.perform(taskDoneEvent -> {
      final JsonArray result = taskDoneEvent.result();

      final Future<Set<String>> processedLinksFuture = processedLinks(vertx, startFuture);

      processedLinksFuture.setHandler(processedLinksEvent -> {
        if (processedLinksEvent.succeeded()) {

          final Set<String> keys = processedLinksEvent.result();

          final List<JsonObject> list = result.getList();
          // filters out already processed links and create requests for rest
          list.stream()
              .map(JsonObject.class::cast)
              .map(entries -> entries.getString("oponent-link"))
              .filter(link -> !keys.contains(link))
              .forEach(linkToProcess -> {

                final TaskDescription taskDescription = TaskDescription
                    .create(PAGE_REQUEST_TASK, new JsonObject()
                        .put("link", linkToProcess));

                vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescription));
              });

          // save parsed results

          final JsonObject fightSaveTaskParams = new JsonObject()
              .put("data", result)
              .put("original-link", originalLink);

          vertx.eventBus().send("task-submit", JsonObject.mapFrom(
              TaskDescription.create(FightSaveTask.FIGHT_SAVE_TASK, fightSaveTaskParams)));


        } else {
          startFuture.fail(processedLinksEvent.cause());
        }
      });
    });
  }

  @Override
  public void perform(Handler<AsyncResult<JsonArray>> handler) {
    LOG.info("Parsing {}", originalLink);
    final Document document = Jsoup.parse(content);

    final Elements profesionalRecordTable = document.select(profesionalMmaRecord())
        .select(fighterRecord())
        .get(0)// get first table - its the pro record
        .select(tableBody())
        .select(tableRow());

    final JsonArray processed = new JsonArray(profesionalRecordTable.stream()
        .filter(PageParseTask::noFutureFights)
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
