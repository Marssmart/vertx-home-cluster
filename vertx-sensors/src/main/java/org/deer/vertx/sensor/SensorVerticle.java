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

package org.deer.vertx.sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.common.NodeReporter;
import org.deer.vertx.cluster.common.dto.ClusterNode;

public class SensorVerticle extends AbstractVerticle {

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      vertx.deployVerticle("org.deer.vertx.sensor.OsVerticle");
      new NodeReporter(vertx).reportNodeStarted(new ClusterNode().setName("os-node"));
    });
  }
}
