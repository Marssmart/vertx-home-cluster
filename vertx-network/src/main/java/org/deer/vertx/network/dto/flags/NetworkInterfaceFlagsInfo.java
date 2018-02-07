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

package org.deer.vertx.network.dto.flags;

import java.net.NetworkInterface;
import java.net.SocketException;

public class NetworkInterfaceFlagsInfo {

  private final boolean loopback;
  private final boolean pointToPoint;
  private final boolean up;
  private final boolean virtual;

  public NetworkInterfaceFlagsInfo(final NetworkInterface iface) {
    try {
      loopback = iface.isLoopback();
    } catch (SocketException e) {
      throw new IllegalStateException("Unable to get is-loopback flag", e);
    }

    try {
      pointToPoint = iface.isPointToPoint();
    } catch (SocketException e) {
      throw new IllegalStateException("Unable to get is-point-to-point flag", e);
    }

    try {
      up = iface.isUp();
    } catch (SocketException e) {
      throw new IllegalStateException("Unable to get is-loopback flag", e);
    }

    virtual = iface.isVirtual();
  }

  public boolean isLoopback() {
    return loopback;
  }

  public boolean isPointToPoint() {
    return pointToPoint;
  }

  public boolean isUp() {
    return up;
  }

  public boolean isVirtual() {
    return virtual;
  }
}
