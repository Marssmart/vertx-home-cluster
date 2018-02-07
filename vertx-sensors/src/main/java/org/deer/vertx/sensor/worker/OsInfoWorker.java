package org.deer.vertx.sensor.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.json.SystemInfo;

public class OsInfoWorker extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(OsInfoWorker.class);

  private final String replyAddress;

  public OsInfoWorker(String replyAddress) {
    this.replyAddress = replyAddress;
  }

  @Override
  public void start() throws Exception {
    LOG.info("Requesting OS info");
    final JsonObject osData = new JsonObject(
        new SystemInfo().getOperatingSystem().toCompactJSON());
    LOG.info("OS info retrieved, replying");
    final String deploymentID = vertx.getOrCreateContext().deploymentID();

    vertx.eventBus().send(replyAddress, osData);
    LOG.info("Undeploying OS info worker");
    vertx.undeploy(deploymentID);
  }
}
