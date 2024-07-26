/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.cluster;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Helper class to handle Openshift token authentication response
 */
public class TokenExtractor {

  private final Document doc;

  public TokenExtractor(String content) {
    doc = Jsoup.parse(content);
  }

  public boolean isTokenPage() {
    return doc.select("h2").text().contains("Your API token is");
  }

  public String getToken() {
    return doc.select("h2").next().select("code").text();
  }
}
