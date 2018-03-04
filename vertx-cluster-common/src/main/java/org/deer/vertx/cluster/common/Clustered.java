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

package org.deer.vertx.cluster.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public interface Clustered {

  static void startClusteredVertx(Handler<AsyncResult<Vertx>> handler) {
    Vertx.clusteredVertx(defaultOptions(),
        handler);
  }

  static VertxOptions defaultOptions() {
    return new VertxOptions().setClusterManager(new HazelcastClusterManager())
        .setWorkerPoolSize(4)
        .setEventLoopPoolSize(4)
        .setInternalBlockingPoolSize(4)
        .setBlockedThreadCheckInterval(1000 * 60 * 60)
        .setMaxWorkerExecuteTime(100000000L)
        .setMaxEventLoopExecuteTime(100000000L)
        .setWarningExceptionTime(100000000L);
  }
}
