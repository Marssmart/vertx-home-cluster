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
