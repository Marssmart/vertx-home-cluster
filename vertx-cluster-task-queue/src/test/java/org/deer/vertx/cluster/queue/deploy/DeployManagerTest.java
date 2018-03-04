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

import static org.deer.vertx.cluster.queue.deploy.DeployManager.DeploymentState.DEPLOYED;
import static org.deer.vertx.cluster.queue.deploy.DeployManager.DeploymentState.LOCKED;
import static org.deer.vertx.cluster.queue.deploy.DeployManager.DeploymentState.SKIPPED;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Lock;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import java.util.concurrent.TimeUnit;
import org.deer.vertx.cluster.queue.deploy.verticle.TestVerticle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(VertxExtension.class)
public class DeployManagerTest implements DeployManager {

  private static final Logger LOG = LoggerFactory.getLogger(DeployManagerTest.class);
  private static final String DEPLOY_LOCK = "TEST-DEPLOY-LOCK";
  private static final String DEPLOY_MARKER = "TEST-DEPLOY-MARKER";

  private Future<Vertx> vertxFuture = Future.future();

  @BeforeEach
  public void init() {
    Vertx.clusteredVertx(new VertxOptions().setClusterManager(new HazelcastClusterManager()),
        vertxFuture);
  }

  @AfterEach
  public void close() {
    vertxFuture.result().close();
  }

  @Test
  public void testDeployNoBlockingCondition() throws InterruptedException {
    VertxTestContext ctx = new VertxTestContext();

    vertxFuture.setHandler(vertxAsyncResult -> {
      final Vertx vertx = vertxAsyncResult.result();

      deployClusterSingleton(vertx, "TEST-DEPLOY-MARKER", DEPLOY_LOCK,
          TestVerticle.class.getName())
          .compose(deploymentState -> verifyState(deploymentState, DEPLOYED))
          .compose(deployId -> getDeployRegistry(vertx))
          .compose(DeployManagerTest::getDeployMarker)
          .compose(DeployManagerTest::verifyDeployMarkerNotNull)
          .map(o -> {
            ctx.completeNow();
            return null;
          })
          .otherwise(throwable -> {
            ctx.failNow(throwable);
            return null;
          });
    });

    ctx.awaitCompletion(60, TimeUnit.SECONDS);
  }

  @Test
  public void testDeployMarkerAllreadySetCondition() throws InterruptedException {
    VertxTestContext ctx = new VertxTestContext();

    vertxFuture.setHandler(vertxAsyncResult -> {
      final Vertx vertx = vertxAsyncResult.result();

      getDeployRegistry(vertx)
          .compose(DeployManagerTest::setDeployMarker)
          .compose(aVoid -> deployClusterSingleton(vertx,
              "TEST-DEPLOY-MARKER", DEPLOY_LOCK,
              TestVerticle.class.getName()))
          .compose(deploymentState -> verifyState(deploymentState, SKIPPED))
          .map(o -> {
            ctx.completeNow();
            return null;
          })
          .otherwise(throwable -> {
            ctx.failNow(throwable);
            return null;
          });
    });

    ctx.awaitCompletion(60, TimeUnit.SECONDS);
  }

  @Test
  public void testDeployLockAllreadyAquiredCondition() throws InterruptedException {
    VertxTestContext ctx = new VertxTestContext();

    vertxFuture.setHandler(vertxAsyncResult -> {
      final Vertx vertx = vertxAsyncResult.result();

      final Future<Lock> lockFuture = Future.future();
      vertx.sharedData().getLock(DEPLOY_LOCK, lockFuture);
      lockFuture.compose(aVoid -> deployClusterSingleton(vertx, DEPLOY_MARKER, DEPLOY_LOCK,
          TestVerticle.class.getName()))
          .compose(deploymentState -> verifyState(deploymentState, LOCKED))
          .map(o -> {
            ctx.completeNow();
            return null;
          })
          .otherwise(throwable -> {
            ctx.failNow(throwable);
            return null;
          });
    });
    ctx.awaitCompletion(60, TimeUnit.SECONDS);
  }

  private static Future<DeploymentState> verifyState(DeploymentState actual,
      DeploymentState desired) {
    LOG.info("Verifying expected state");
    if (actual != desired) {
      return Future
          .failedFuture(new IllegalStateException("Unexpected state " + actual));
    } else {
      return Future.succeededFuture(desired);
    }
  }

  private static Future<Void> setDeployMarker(AsyncMap<Object, Object> deployRegistry) {
    final Future<Void> future = Future.future();
    deployRegistry.put("TEST-DEPLOY-MARKER", true, future);
    return future;
  }

  private static Future<Object> verifyDeployMarkerNotNull(Object deployMarker) {
    LOG.info("Verifying deploy marker not null");
    if (deployMarker != null) {
      return Future.succeededFuture();
    } else {
      return Future.failedFuture(
          new IllegalStateException("Verticle not marked as deployed"));
    }
  }

  private static Future<Object> getDeployMarker(AsyncMap<Object, Object> deployRegistry) {
    final Future<Object> deployMarkerFuture = Future.future();
    deployRegistry.get("TEST-DEPLOY-MARKER", deployMarkerFuture);
    return deployMarkerFuture;
  }

  private static Future<AsyncMap<Object, Object>> getDeployRegistry(Vertx vertx) {
    final Future<AsyncMap<Object, Object>> mapFuture = Future.future();
    vertx.sharedData().getClusterWideMap(DEPLOY_REGISTRY, mapFuture);
    return mapFuture;
  }

}