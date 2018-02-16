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

package org.deer.vertx.network;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.deer.vertx.cluster.common.Clustered;
import org.deer.vertx.cluster.common.NodeReporter;
import org.deer.vertx.cluster.common.dto.ClusterNode;
import org.deer.vertx.network.dto.NetworkInterfaceInfo;
import org.deer.vertx.network.dto.address.NetworkInterfaceAddressesInfo;
import org.deer.vertx.network.dto.flags.NetworkInterfaceFlagsInfo;

public class NetworkVerticle extends AbstractVerticle {

  public static final int ERROR_INTERFACE_INFO_FAILED = 1;
  public static final int ERROR_INTERFACE_INDEX_NOT_FOUND = 2;

  private NodeReporter nodeReporter;

  public static void main(String[] args) {
    Clustered.startClusteredVertx(event -> {
      final Vertx vertx = event.result();

      vertx.deployVerticle("org.deer.vertx.network.NetworkVerticle");
    });
  }

  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("network-device-info")
        .handler(networkDeviceInfoRequest -> {
          try {
            networkDeviceInfoRequest
                .reply(new JsonArray(Collections.list(NetworkInterface.getNetworkInterfaces())
                    .stream()
                    .map(NetworkInterfaceInfo::new)
                    .collect(Collectors.toList())));
          } catch (SocketException e) {
            networkDeviceInfoRequest.fail(ERROR_INTERFACE_INFO_FAILED, e.getMessage());
          }
        });

    vertx.eventBus().consumer("network-device-flags-info")
        .handler(networkDeviceInfoRequest -> {
          final Integer index = Integer.class.cast(networkDeviceInfoRequest.body());
          try {

            final Optional<NetworkInterface> interfaceByIndex = Collections
                .list(NetworkInterface.getNetworkInterfaces())
                .stream()
                .filter(networkInterface -> networkInterface.getIndex() == index)
                .findFirst();

            if (interfaceByIndex.isPresent()) {
              networkDeviceInfoRequest
                  .reply(JsonObject.mapFrom(new NetworkInterfaceFlagsInfo(interfaceByIndex.get())));
            } else {
              networkDeviceInfoRequest
                  .fail(ERROR_INTERFACE_INDEX_NOT_FOUND, "Interface index not found");
            }
          } catch (SocketException e) {
            networkDeviceInfoRequest.fail(ERROR_INTERFACE_INFO_FAILED, e.getMessage());
          }
        });

    vertx.eventBus().consumer("network-device-address-info")
        .handler(networkDeviceAddressInfoRequest -> {
          final Integer index = Integer.class.cast(networkDeviceAddressInfoRequest.body());

          try {
            final Optional<NetworkInterface> interfaceByIndex = Collections
                .list(NetworkInterface.getNetworkInterfaces())
                .stream()
                .filter(networkInterface -> networkInterface.getIndex() == index)
                .findFirst();

            if (interfaceByIndex.isPresent()) {
              networkDeviceAddressInfoRequest
                  .reply(JsonObject
                      .mapFrom(new NetworkInterfaceAddressesInfo(interfaceByIndex.get())));
            } else {
              networkDeviceAddressInfoRequest
                  .fail(ERROR_INTERFACE_INDEX_NOT_FOUND, "Interface index not found");
            }

          } catch (SocketException e) {
            networkDeviceAddressInfoRequest.fail(ERROR_INTERFACE_INFO_FAILED, e.getMessage());
          }
        });
    nodeReporter = new NodeReporter(vertx);
    nodeReporter.reportNodeStarted(new ClusterNode().setName("network-node"));
  }

  @Override
  public void stop() throws Exception {
    nodeReporter.close();
  }
}
