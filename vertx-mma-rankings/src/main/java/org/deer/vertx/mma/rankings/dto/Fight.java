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

package org.deer.vertx.mma.rankings.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Fight {

  private final long fighterRef;
  private final String date;
  private final String oponentLink;
  private final String oponentFirstName;
  private final String oponentLastName;
  private final String event;
  private final String fightEnd;
  private final String fightEndType;
  private final String stopageRound;
  private final String stopageTime;

  @JsonCreator
  public Fight(@JsonProperty("fighter-ref") final long fighterRef,
      @JsonProperty("date") final String date,
      @JsonProperty("oponent-link") final String oponentLink,
      @JsonProperty("oponent-first-name") final String oponentFirstName,
      @JsonProperty("oponent-last-name") final String oponentLastName,
      @JsonProperty("event") final String event,
      @JsonProperty("fight-end") final String fightEnd,
      @JsonProperty("fight-end-type") final String fightEndType,
      @JsonProperty("stopage-round") final String stopageRound,
      @JsonProperty("stopage-time") final String stopageTime) {
    this.fighterRef = fighterRef;
    this.date = date;
    this.oponentLink = oponentLink;
    this.oponentFirstName = oponentFirstName;
    this.oponentLastName = oponentLastName;
    this.event = event;
    this.fightEnd = fightEnd;
    this.fightEndType = fightEndType;
    this.stopageRound = stopageRound;
    this.stopageTime = stopageTime;
  }

  public long getFighterRef() {
    return fighterRef;
  }

  public String getDate() {
    return date;
  }

  public String getOponentLink() {
    return oponentLink;
  }

  public String getOponentFirstName() {
    return oponentFirstName;
  }

  public String getOponentLastName() {
    return oponentLastName;
  }

  public String getEvent() {
    return event;
  }

  public String getFightEnd() {
    return fightEnd;
  }

  public String getFightEndType() {
    return fightEndType;
  }

  public String getStopageRound() {
    return stopageRound;
  }

  public String getStopageTime() {
    return stopageTime;
  }
}
