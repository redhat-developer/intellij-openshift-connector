/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.cluster;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenExtractorTest {

  @Test
  public void checkTokenExtractionOk() {
    String contents = "<html><head></head><body><h2>Your API token is</h2>\n  <code>sha256~abcd-1234567890ABCDEF</code>\n</body></html>";
    TokenExtractor extractor = new TokenExtractor(contents);
    assertTrue(extractor.isTokenPage());
    assertEquals("sha256~abcd-1234567890ABCDEF", extractor.getToken());
  }

  @Test
  public void checkTokenExtractionEmpty() {
    String contents = "<html><head></head><body><h2>Your API token is</h2>\n  <code></code>\n</body></html>";
    TokenExtractor extractor = new TokenExtractor(contents);
    assertTrue(extractor.isTokenPage());
    assertEquals("", extractor.getToken());
  }

  @Test
  public void checkTokenExtractionFails() {
    String contents = "<html><head></head><body></body></html>";
    TokenExtractor extractor = new TokenExtractor(contents);
    assertFalse(extractor.isTokenPage());
  }

  @Test
  public void checkTokenExtractionWithMultipleH2() {
    String contents = "<html><head></head><body><h2>Your API token is</h2>\n  <code>sha256~abcd-1234567890ABCDEF</code>\n<h2>Log in with this token</h2>\n<pre>oc login <span class=\"nowrap\">--token=sha256~abcd-1234567890ABCDEF</span> <span class=\"nowrap\">--server=https://url.com:1234</span></pre></body></html>";
    TokenExtractor extractor = new TokenExtractor(contents);
    assertTrue(extractor.isTokenPage());
    assertEquals("sha256~abcd-1234567890ABCDEF", extractor.getToken());
  }
}
