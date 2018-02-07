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

package org.deer.vertx.sensor.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.json.SystemInfo;

public class OsInfoWorker extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OsInfoWorker.class);

  private final String replyAddress;

  public OsInfoWorker(String replyAddress) {
    this.replyAddress = replyAddress;
  }

  @Override
  public void start() throws Exception {
    LOG.info("Requesting OS info");
    final JsonObject osData = new JsonObject(
        new SystemInfo().getOperatingSystem().toCompactJSON());
    LOG.info("OS info retrieved, replying");
    final String deploymentID = vertx.getOrCreateContext().deploymentID();

    vertx.eventBus().send(replyAddress, osData);
    LOG.info("Undeploying OS info worker");
    vertx.undeploy(deploymentID);
  }
}
