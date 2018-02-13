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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import java.util.List;
import java.util.stream.Collectors;
import org.deer.vertx.cluster.common.dto.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeReporter {

  private static final Logger LOG = LoggerFactory.getLogger(NodeReporter.class);

  private static final String DEFAULT_NODE_MAP_NAME = "cluster-nodes-map";

  private final Vertx vertx;
  private final String nodeMapName;

  public NodeReporter(Vertx vertx) {
    this.vertx = vertx;
    this.nodeMapName = DEFAULT_NODE_MAP_NAME;
  }

  public NodeReporter(Vertx vertx, String nodeMapName) {
    this.vertx = vertx;
    this.nodeMapName = nodeMapName;
  }

  public Future<Void> reportNodeStarted(final ClusterNode node) {
    final Future<Void> reportFuture = Future.future();
    vertx.sharedData().getClusterWideMap(nodeMapName, mapEvent -> {
      if (mapEvent.succeeded()) {
        node.setShutdownAddress(registerShutdownEvent(vertx, node.getTimeCreated()));

        final AsyncMap<Object, Object> clusterNodes = mapEvent.result();
        clusterNodes.put(node.getTimeCreated(), JsonObject.mapFrom(node).encode(), putEvent -> {
          if (putEvent.succeeded()) {
            reportFuture.complete();
            LOG.info("Node {} successfully reported to cluster management", node.getName());
          } else {
            reportFuture.fail(putEvent.cause());
          }
        });
      } else {
        reportFuture.fail(mapEvent.cause());
      }
    });
    return reportFuture;
  }

  private Future<Void> unregisterNode(final long key) {
    final Future<Void> reportFuture = Future.future();
    vertx.sharedData().getClusterWideMap(nodeMapName, mapEvent -> {
      if (mapEvent.succeeded()) {

        final AsyncMap<Object, Object> clusterNodes = mapEvent.result();
        clusterNodes.remove(key, putEvent -> {
          if (putEvent.succeeded()) {
            reportFuture.complete();
            LOG.info("Node with key {} successfully unregistered from cluster management", key);
          } else {
            reportFuture.fail(putEvent.cause());
          }
        });
      } else {
        reportFuture.fail(mapEvent.cause());
      }
    });
    return reportFuture;
  }

  private String registerShutdownEvent(final Vertx vertx, final long id) {
    String shutdownAddress = id + "-shutdown";
    vertx.eventBus().consumer(shutdownAddress).handler(event -> {
      unregisterNode(id).setHandler(unregisterEvent -> {
        vertx.close();
        vertx.nettyEventLoopGroup().shutdownGracefully()
            .addListener(future -> System.exit(0));

      });
    });
    return shutdownAddress;
  }

  public Future<List<ClusterNode>> reportRunningNodes() {
    final Future<List<ClusterNode>> activeNodes = Future.future();
    vertx.sharedData().getClusterWideMap(nodeMapName, mapEvent -> {
      if (mapEvent.succeeded()) {
        final AsyncMap<Object, Object> clusterNodesMap = mapEvent.result();

        clusterNodesMap.values(valuesEvent -> {
          if (valuesEvent.succeeded()) {
            activeNodes.complete(valuesEvent.result().stream()
                .map(String.class::cast)
                .map(JsonObject::new)
                .map(json -> json.mapTo(ClusterNode.class))
                .collect(Collectors.toList()));
          } else {
            activeNodes.fail(valuesEvent.cause());
          }
        });
      } else {
        activeNodes.fail(mapEvent.cause());
      }
    });
    return activeNodes;
  }
}
