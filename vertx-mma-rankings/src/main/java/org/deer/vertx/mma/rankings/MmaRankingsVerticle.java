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

package org.deer.vertx.mma.rankings;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.queue.deploy.DeployManager;
import org.deer.vertx.mma.rankings.task.download.and.save.link.scenario.FighterProfileResultProcessorVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmaRankingsVerticle extends AbstractVerticle implements DeployManager {

  private static final Logger LOG = LoggerFactory.getLogger(MmaRankingsVerticle.class);

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      vertx.deployVerticle("org.deer.vertx.mma.rankings.MmaRankingsVerticle");
    });
  }

  @Override
  public void start() throws Exception {
    //todo - for test
    deployClusterSingleton(vertx, "test-http-server", "test-http-server-lock",
        TestHttpServerVerticle.class.getName());

    deployClusterSingleton(vertx, "fighter-profile-link-processor",
        "fighter-profile-link-processor-lock",
        FighterProfileResultProcessorVerticle.class.getName());

    vertx.deployVerticle(TaskPerformerVerticle.class.getName(),
        new DeploymentOptions().setInstances(4));
  }
}
