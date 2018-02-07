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

package org.deer.vertx.network.dto;

import java.net.NetworkInterface;

public class NetworkInterfaceInfo {

  private final int index;
  private final String name;
  private final String displayName;

  public NetworkInterfaceInfo(final NetworkInterface iface) {
    index = iface.getIndex();
    name = iface.getName();
    displayName = iface.getDisplayName();
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }
}
