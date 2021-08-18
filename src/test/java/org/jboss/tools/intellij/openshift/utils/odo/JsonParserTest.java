/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class JsonParserTest {
  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = new ObjectMapper();
  }

  @Test
  public void verifyThatCRDCanBeLoader() throws IOException {
    URL url = JsonParserTest.class.getResource("/swagger.json");
    JSonParser parser = new JSonParser(MAPPER.readTree(url));
    JsonNode schema = parser.findSchema("/apis/kafka.strimzi.io/v1beta2/namespaces/{namespace}/kafkas");
    assertNotNull(schema);
  }
}
