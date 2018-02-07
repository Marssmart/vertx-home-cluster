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

package org.deer.vertx.network.dto.address;

import static java.lang.String.format;

import io.vertx.core.json.JsonArray;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.stream.Collectors;

public class NetworkInterfaceAddressesInfo {

  private final int mtu;
  private String hardwareAddress;
  private final JsonArray addresses;
  private final JsonArray binding;

  public NetworkInterfaceAddressesInfo(final NetworkInterface iface) {
    try {
      mtu = iface.getMTU();
    } catch (SocketException e) {
      throw new IllegalStateException(format("Unable to get MTU for %s", iface.getName()), e);
    }

    try {
      final byte[] hardwareAddress = iface.getHardwareAddress();
      if (hardwareAddress != null) {
        this.hardwareAddress = macToString(hardwareAddress);
      }
    } catch (SocketException e) {
      throw new IllegalStateException(format("Unable to get MTU for %s", iface.getName()), e);
    }

    addresses = new JsonArray(Collections.list(iface.getInetAddresses())
        .stream()
        .map(InetAddress::getHostAddress)
        .collect(Collectors.toList()));

    binding = new JsonArray(iface.getInterfaceAddresses()
        .stream()
        .map(BindingAddressInfo::new)
        .collect(Collectors.toList()));
  }

  public int getMtu() {
    return mtu;
  }

  public String getHardwareAddress() {
    return hardwareAddress;
  }

  public JsonArray getAddresses() {
    return addresses;
  }

  public JsonArray getBinding() {
    return binding;
  }

  private static String macToString(final byte[] mac) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }
    return sb.toString();
  }
}
