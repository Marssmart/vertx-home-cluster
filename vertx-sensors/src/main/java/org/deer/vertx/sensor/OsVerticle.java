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
