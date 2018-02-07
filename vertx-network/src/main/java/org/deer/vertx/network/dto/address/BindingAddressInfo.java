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

import java.net.InetAddress;
import java.net.InterfaceAddress;

public class BindingAddressInfo {

  private final String address;
  private String broadcast;
  private final short prefix;

  public BindingAddressInfo(final InterfaceAddress ifaceAddress) {
    address = ifaceAddress.getAddress().getHostAddress();

    final InetAddress broadcastAddress = ifaceAddress.getBroadcast();
    if (broadcastAddress != null) {
      this.broadcast = broadcastAddress.getHostAddress();
    }
    prefix = ifaceAddress.getNetworkPrefixLength();
  }

  public String getAddress() {
    return address;
  }

  public String getBroadcast() {
    return broadcast;
  }

  public short getPrefix() {
    return prefix;
  }
}
