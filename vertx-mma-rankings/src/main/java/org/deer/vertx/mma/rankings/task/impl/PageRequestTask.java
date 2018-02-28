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

import static com.google.common.base.Preconditions.checkState;
import static org.deer.vertx.cluster.queue.task.TaskDescription.TaskPriority.MEDIUM;
import static org.deer.vertx.mma.rankings.task.impl.PageParseTask.PAGE_PARSE_TASK;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.TaskDescription;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageRequestTask extends AbstractTaskExecutor<HtmlPage>
    implements HttpClient, ProcessedLinksRegistryAccessor, TaskSubmitter {

  private static final Logger LOG = LoggerFactory.getLogger(PageRequestTask.class);

  public static final String PAGE_REQUEST_TASK = "page-request-task";

  private final String link;

  public PageRequestTask(final JsonObject params) {
    super(PAGE_REQUEST_TASK);
    checkState(params.containsKey("link"), "Link not specified");
    this.link = params.getString("link");
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    this.perform(event -> {
      if (event.succeeded()) {
        final HtmlPage page = event.result();

        final Future<Void> pageProcessedFuture = markLinkProcessed(vertx,
            page.getBaseURL().toString(),
            startFuture);

        pageProcessedFuture.setHandler(pageProcessedEvent -> {
          if (pageProcessedEvent.succeeded()) {
            LOG.info("Processing of link {} done", page.getBaseURL().toString());
            final TaskDescription taskDescription = createTaskDescriptor(PAGE_PARSE_TASK,
                createPageParseParams(page), MEDIUM);

            // HtmlPage is memory heavy,
            // so cleaning up directly after getting xml content
            page.cleanUp();

            vertx.eventBus().send("task-submit", JsonObject.mapFrom(taskDescription));

            startFuture.complete();
          } else {
            startFuture.fail(pageProcessedEvent.cause());
          }
        });
      } else {
        startFuture.fail(event.cause());
      }
    });
  }

  @Override
  public void perform(Handler<AsyncResult<HtmlPage>> handler) {
    HtmlPage page = null;
    try (WebClient client = createClient()) {
      page = client.getPage(link);
    } catch (IOException e) {
      handler.handle(Future.failedFuture(e));
    }

    handler.handle(Future.succeededFuture(page));
  }

  private static JsonObject createPageParseParams(HtmlPage page) {
    return new JsonObject()
        .put("original-link", page.getBaseURL().toString())
        .put("page-content", page.asXml());
  }
}
