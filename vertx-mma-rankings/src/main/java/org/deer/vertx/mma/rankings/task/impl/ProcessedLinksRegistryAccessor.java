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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import java.util.Set;
import java.util.stream.Collectors;

public interface ProcessedLinksRegistryAccessor {

  String PROCESSED_LINKS_REGISTRY = "processed-mma-profile-links";

  default Future<Void> markLinkProcessed(final Vertx vertx,
      final String link,
      final Future<Void> failureHandler) {
    final Future<AsyncMap<Object, Object>> processedLinksRegistry = Future.future();
    vertx.sharedData().getClusterWideMap(PROCESSED_LINKS_REGISTRY, processedLinksRegistry);

    final Future<Void> pageProcessedFuture = Future.future();
    processedLinksRegistry.setHandler(registryEvent -> {
      if (registryEvent.succeeded()) {
        registryEvent.result().put(link, true, pageProcessedFuture);
      } else {
        failureHandler.fail(registryEvent.cause());
      }
    });

    return pageProcessedFuture;
  }

  default Future<Set<String>> processedLinks(final Vertx vertx,
      final Future<Void> failureHandler) {
    final Future<AsyncMap<Object, Object>> processedLinksRegistry = Future.future();
    vertx.sharedData().getClusterWideMap(PROCESSED_LINKS_REGISTRY, processedLinksRegistry);

    final Future<Set<Object>> keysFuture = Future.future();
    processedLinksRegistry.setHandler(registryEvent -> {
      if (registryEvent.succeeded()) {
        registryEvent.result().keys(keysFuture);
      } else {
        failureHandler.fail(registryEvent.cause());
      }
    });

    return keysFuture.map(objects -> objects.stream()
        .map(String.class::cast)
        .collect(Collectors.toSet()));
  }
}
