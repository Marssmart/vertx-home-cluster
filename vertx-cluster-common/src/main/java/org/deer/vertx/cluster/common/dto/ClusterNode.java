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

package org.deer.vertx.cluster.common.dto;

public class ClusterNode {

  private long timeCreated;
  private String name;
  private String shutdownAddress;

  public ClusterNode() {
    timeCreated = System.currentTimeMillis();
  }

  public String getName() {
    return name;
  }

  public long getTimeCreated() {
    return timeCreated;
  }

  public ClusterNode setName(String name) {
    this.name = name;
    return this;
  }

  public ClusterNode setTimeCreated(long timeCreated) {
    this.timeCreated = timeCreated;
    return this;
  }

  public String getShutdownAddress() {
    return shutdownAddress;
  }

  public void setShutdownAddress(String shutdownAddress) {
    this.shutdownAddress = shutdownAddress;
  }
}
