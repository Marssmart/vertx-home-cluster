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

import static com.google.common.base.Preconditions.checkState;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import org.deer.vertx.cluster.queue.task.AbstractTaskExecutor;
import org.deer.vertx.cluster.queue.task.QueuedTask;
import org.deer.vertx.cluster.queue.task.TaskStatsUpdater;
import org.deer.vertx.cluster.queue.task.TaskSubmitter;
import org.deer.vertx.mma.rankings.http.HttpClient;
import org.deer.vertx.mma.rankings.task.impl.ProcessedLinksRegistryAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that request fighter profile from www.mixedmartialarts.com and submits parsing task for it
 */
public abstract class PageRequestTask extends AbstractTaskExecutor<HtmlPage>
    implements HttpClient, ProcessedLinksRegistryAccessor, TaskSubmitter, TaskStatsUpdater {

  private static final Logger LOG = LoggerFactory.getLogger(PageRequestTask.class);

  protected final QueuedTask task;

  public PageRequestTask(final QueuedTask task, final String taskType) {
    super(taskType);
    checkState(task.getDescription().parseParams().containsKey("link"), "Link not specified");
    this.task = task;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    reportTaskStarted(vertx, task);
    final String deploymentId = vertx.getOrCreateContext().deploymentID();
    this.perform(event -> {
      if (event.succeeded()) {
        final HtmlPage page = event.result();

        final Future<Void> pageProcessedFuture = markFighterNameProcessed(vertx,
            page.getBaseURL().toString(),
            startFuture);

        pageProcessedFuture.setHandler(pageProcessedEvent -> {
          if (pageProcessedEvent.succeeded()) {
            LOG.info("Processing of link {} done", page.getBaseURL().toString());
            onPageRequestFinished(page);

            // HtmlPage is memory heavy,
            // so cleaning up directly after getting xml content
            page.cleanUp();

            startFuture.complete();
            reportTaskFinished(vertx, task);
            vertx.undeploy(deploymentId);
          } else {
            reportTaskFailed(vertx, task, pageProcessedEvent.cause().getMessage());
            startFuture.fail(pageProcessedEvent.cause());
          }
        });
      } else {
        reportTaskFailed(vertx, task, event.cause().getMessage());
        startFuture.fail(event.cause());
      }
    });
  }

  @Override
  public void perform(Handler<AsyncResult<HtmlPage>> handler) {
    HtmlPage page = null;
    try (WebClient client = createClient()) {
      page = client.getPage(task.getDescription().parseParams().getString("link"));
    } catch (IOException e) {
      handler.handle(Future.failedFuture(e));
    }

    handler.handle(Future.succeededFuture(page));
  }

  protected static JsonObject createPageParseParams(HtmlPage page) {
    return new JsonObject()
        .put("original-link", page.getBaseURL().toString())
        .put("page-content", page.asXml());
  }

  public abstract void onPageRequestFinished(HtmlPage page);
}
