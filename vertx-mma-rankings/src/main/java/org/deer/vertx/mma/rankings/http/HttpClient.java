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

package org.deer.vertx.mma.rankings.http;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import java.util.logging.Level;
import org.apache.commons.logging.LogFactory;

public interface HttpClient {

  default WebClient createClient() {
    LogFactory.getFactory()
        .setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

    final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_52);
    webClient.getOptions().setCssEnabled(false);
    webClient.setCssErrorHandler(new NoopErrorHandler());
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    webClient.waitForBackgroundJavaScript(5000);
    webClient.setJavaScriptTimeout(5000);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    return webClient;
  }
}
