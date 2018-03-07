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

public final class Fighter {

  private final Long ref;
  private final String firstName;
  private final String lastName;
  private final String profileLink;

  @JsonCreator
  public Fighter(@JsonProperty("ref") final Long ref,
      @JsonProperty("firstName") final String firstName,
      @JsonProperty("lastName") final String lastName,
      @JsonProperty("profileLink") final String profileLink) {
    this.ref = ref;
    this.firstName = firstName;
    this.lastName = lastName;
    this.profileLink = profileLink;
  }

  public String getProfileLink() {
    return profileLink;
  }

  public Long getRef() {
    return ref;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
