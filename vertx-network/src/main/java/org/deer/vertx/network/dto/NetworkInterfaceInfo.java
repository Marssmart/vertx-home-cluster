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
