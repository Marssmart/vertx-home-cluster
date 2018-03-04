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

package org.deer.vertx.cluster.common.mongo;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;

public interface MongoClientUser {

  default Future<MongoClient> connectToMongo(final Vertx vertx) {
    final Future<MongoClient> clientFuture = Future.future();
    vertx.fileSystem().readFile("mongo.config", result -> clientFuture
        .complete(MongoClient.createShared(vertx, result.result().toJsonObject())));
    return clientFuture;
  }

  default Future<MongoClient> connectDedicatedToMongo(final Vertx vertx) {
    final Future<MongoClient> clientFuture = Future.future();
    vertx.fileSystem().readFile("mongo.config", result -> clientFuture
        .complete(MongoClient.createNonShared(vertx, result.result().toJsonObject())));
    return clientFuture;
  }
}
