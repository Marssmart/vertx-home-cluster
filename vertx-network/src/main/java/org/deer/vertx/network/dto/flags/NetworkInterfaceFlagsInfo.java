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
