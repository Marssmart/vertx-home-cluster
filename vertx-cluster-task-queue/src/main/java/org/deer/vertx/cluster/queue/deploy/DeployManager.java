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

package org.deer.vertx.cluster.queue.deploy;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Lock;
import java.util.Set;
import org.slf4j.Logger;

public interface DeployManager {

  DeployManager INSTANCE = new DeployManager() {
  };
  String DEPLOY_REGISTRY = "deploy-registry";

  /**
   * Calls deploy function
   */
  default Future<String> deployClusterSingleton(final Vertx vertx,
      final String deployMarkerName,
      final String deployLock,
      final String verticleName,
      final Logger logger) {

    final Future<String> deployFuture = Future.future();

    // retrieve deployment marker map
    final Future<AsyncMap<Object, Object>> mapFuture = Future.future();

    logger.info("Deploying {} as cluster singleton", verticleName);
    logger.info("Retrieving deploy registry {}", DEPLOY_REGISTRY);
    vertx.sharedData().getClusterWideMap(DEPLOY_REGISTRY, mapFuture);

    // get keys
    final Future<Set<Object>> keysFuture = Future.future();
    mapFuture.setHandler(mapEvent -> {
      if (mapEvent.succeeded()) {
        logger.info("Retrieving keys for {}", DEPLOY_REGISTRY);
        mapEvent.result().keys(keysFuture);
      } else {
        deployFuture.fail(mapEvent.cause());
      }
    });

    //check if not already marked as deployed, if not, try to retrieve deploy lock
    final Future<Lock> lockFuture = Future.future();
    keysFuture.setHandler(keysEvent -> {
      if (keysEvent.succeeded()) {
        if (!keysEvent.result().contains(deployMarkerName)) {
          logger.info("Deploy marker not found, retrieving deploy lock {}", deployLock);
          vertx.sharedData().getLock(deployLock, lockFuture);
        } else {
          logger.info("Deploy marker found, skipping deployment of {}", deployLock, verticleName);
          deployFuture.complete();
        }
      } else {
        deployFuture.fail(keysEvent.cause());
      }
    });

    // if able to retrieve lock, deploy verticle
    lockFuture.setHandler(lockEvent -> {
      if (lockEvent.succeeded()) {
        logger.info("Deploy lock {} retrieved, deploying verticle {}", deployLock, verticleName);
        vertx.deployVerticle(verticleName, deployFuture);
      } else {
        logger.info("Deploy lock already occupied, skipping deployment");
        deployFuture.complete("Deployment-skipped");
      }
    });

    deployFuture.setHandler(deployEvent -> {
      if (deployEvent.succeeded()) {
        logger.info("Verticle {} deployed, marking as deployed", verticleName);
        //if deployment was successful, mark and release lock(even if mark failed)
        mapFuture.result().put(deployMarkerName, true, markEvent -> {
          logger.info("Releasing deploy lock {}", deployLock);
          lockFuture.result().release();
          if (mapFuture.succeeded()) {
            //FIXME - future is already completed here for some reason
            deployFuture.complete();
          } else {
            deployFuture.fail(markEvent.cause());
          }
        });
      } else {
        // if deploy failed, just release lock
        lockFuture.result().release();
        deployFuture.fail(deployEvent.cause());
      }
    });

    return deployFuture;
  }
}
