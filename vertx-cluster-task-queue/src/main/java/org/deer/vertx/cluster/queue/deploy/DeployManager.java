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
import org.slf4j.LoggerFactory;

public interface DeployManager {

  // logger for default methods
  Logger LOG = LoggerFactory.getLogger(DeployManager.class);

  DeployManager INSTANCE = new DeployManager() {
  };
  String DEPLOY_REGISTRY = "deploy-registry";

  /**
   * Calls deploy function
   */
  default Future<DeploymentState> deployClusterSingleton(final Vertx vertx,
      final String deployMarkerName,
      final String deployLock,
      final String verticleName) {

    final Future<DeploymentState> deployFuture = Future.future();

    LOG.info("Deploying {} as cluster singleton", verticleName);

    getDeployLock(vertx, deployLock).setHandler(lockAsyncResult -> {
      if (lockAsyncResult.succeeded()) {

        getDeployRegistry(vertx).setHandler(mapResult -> {
          if (mapResult.succeeded()) {

            getDeployMarkers(mapResult.result()).setHandler(deployMarkersResult -> {
              if (deployMarkersResult.succeeded()) {

                if (!deployMarkersResult.result().contains(deployMarkerName)) {
                  LOG.info("Deploy marker not found, retrieving deploy lock {}",
                      deployLock);

                  startVerticle(vertx, verticleName)
                      .setHandler(stringAsyncResult -> {
                        if (stringAsyncResult.succeeded()) {
                          LOG.info("Verticle {} succesfully deployed", verticleName);

                          markVerticleDeployed(mapResult.result(), deployMarkerName)
                              .setHandler(markResult -> {
                                if (mapResult.succeeded()) {
                                  LOG.info("Verticle {} successfully marked as deployed",
                                      verticleName);
                                } else {
                                  LOG.error("Error marking verticle {} as deployed", verticleName);
                                }
                                lockAsyncResult.result().release();
                              });
                          deployFuture.complete(DeploymentState.DEPLOYED);
                        } else {
                          LOG.error("Error while deploying verticle {}", verticleName,
                              stringAsyncResult.cause());
                          deployFuture.fail(stringAsyncResult.cause());
                          lockAsyncResult.result().release();
                        }
                      });

                } else {
                  LOG.info("Deploy marker found, skipping deployment of {}", deployLock,
                      verticleName);
                  deployFuture.complete(DeploymentState.SKIPPED);
                }
              } else {
                deployFuture.fail(deployMarkersResult.cause());
              }
            });
          } else {
            deployFuture.fail(mapResult.cause());
          }
        });
      } else {
        deployFuture.complete(DeploymentState.LOCKED);
      }
    });

    return deployFuture;
  }

  static Future<Set<Object>> getDeployMarkers(AsyncMap<Object, Object> deployRegistry) {
    LOG.info("Retrieving keys for {}", DEPLOY_REGISTRY);
    // get keys
    final Future<Set<Object>> keysFuture = Future.future();
    deployRegistry.keys(keysFuture);
    return keysFuture;
  }

  static Future<String> startVerticle(Vertx vertx, String verticleName) {
    LOG.info("Starting verticle {}", verticleName);
    final Future<String> startFuture = Future.future();
    vertx.deployVerticle(verticleName, startFuture);
    return startFuture;
  }

  static Future<Lock> getDeployLock(Vertx vertx, String deployLock) {
    LOG.info("Retrieving deploy lock {}", deployLock);
    final Future<Lock> lockFuture = Future.future();
    vertx.sharedData().getLockWithTimeout(deployLock, 2000, lockFuture);
    return lockFuture;
  }

  static Future<AsyncMap<Object, Object>> getDeployRegistry(Vertx vertx) {
    LOG.info("Retrieving deploy registry {}", DEPLOY_REGISTRY);
    final Future<AsyncMap<Object, Object>> mapFuture = Future.future();
    vertx.sharedData().getClusterWideMap(DEPLOY_REGISTRY, mapFuture);
    return mapFuture;
  }

  static Future<Void> markVerticleDeployed(AsyncMap<Object, Object> deployRegistry,
      String deployMarker) {
    final Future<Void> markedFuture = Future.future();
    deployRegistry.put(deployMarker, true, markedFuture);
    return markedFuture;
  }

  enum DeploymentState {
    // if deployment was successful
    DEPLOYED,
    // if unable to get deployment lock
    LOCKED,
    // if already marked as deployed
    SKIPPED
  }
}
