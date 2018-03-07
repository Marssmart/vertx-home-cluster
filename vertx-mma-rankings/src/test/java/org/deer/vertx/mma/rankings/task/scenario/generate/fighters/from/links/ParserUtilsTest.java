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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ParserUtilsTest {

  @Test
  public void testParseFighterName() {
    String link = "http://www.mixedmartialarts.com/fighter/Stipe-Miocic:16A578F1B4D54D98";

    assertEquals("Stipe", ParserUtils.nameFromLink(link));
  }

  @Test
  public void testParseFightersSurname() {
    String link = "http://www.mixedmartialarts.com/fighter/Stipe-Miocic:16A578F1B4D54D98";

    assertEquals("Miocic", ParserUtils.surnameFromLink(link));
  }
}