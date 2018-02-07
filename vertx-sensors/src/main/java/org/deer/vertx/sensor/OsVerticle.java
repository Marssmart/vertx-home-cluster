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
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.MessageConsumer;
import org.deer.vertx.sensor.worker.OsInfoWorker;

public class OsVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("os-info")
        .handler(osInfoRequestEvent -> {
          final String callbackAddress = osInfoRequestEvent.toString();
          final MessageConsumer<Object> txConsumer = vertx.eventBus().consumer(callbackAddress);
          txConsumer.handler(callback -> {
            osInfoRequestEvent.reply(callback.body());
            txConsumer.unregister();
          });

          vertx.deployVerticle(new OsInfoWorker(callbackAddress),
              new DeploymentOptions().setWorker(true));
        });
  }
}
