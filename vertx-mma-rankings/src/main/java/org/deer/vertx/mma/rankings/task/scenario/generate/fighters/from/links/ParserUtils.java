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

package org.deer.vertx.mma.rankings.task.scenario.generate.fighters.from.links;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ParserUtils {

  private ParserUtils() {
  }

  //http://www.mixedmartialarts.com/fighter/Stipe-Miocic:16A578F1B4D54D98
  public static String nameFromLink(final String link) {
    checkNotNull(link, "Link is null");

    final int fighterNameStart = getFighterNameStart(link);

    return link.substring(fighterNameStart, link.indexOf("-", fighterNameStart)).trim();
  }


  public static String surnameFromLink(final String link) {
    checkNotNull(link, "Link is null");

    final int fighterNameStart = getFighterNameStart(link);

    return link
        .substring(link.indexOf("-", fighterNameStart) + 1, link.indexOf(":", fighterNameStart))
        .replace("-", " ").trim();
  }


  private static int getFighterNameStart(String link) {
    return link.indexOf("/fighter/") + "/fighter/".length();
  }
}
