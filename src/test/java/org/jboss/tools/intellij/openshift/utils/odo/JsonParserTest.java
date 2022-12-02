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
import java.util.List;

import static org.junit.Assert.assertEquals;
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

  @Test
  public void verifyThatURLSCanBeLoadedFromDevMode() throws IOException{
    URL url = JsonParserTest.class.getResource("/describe-component-dev.json");
    JSonParser parser = new JSonParser(MAPPER.readTree(url));
    List< org.jboss.tools.intellij.openshift.utils.odo.URL> urls = parser.parseURLS();
    assertNotNull(urls);
    assertEquals(2, urls.size());
    assertEquals("40001", urls.get(0).getLocalPort());
    assertEquals("3000", urls.get(0).getContainerPort());
    assertEquals("runtime", urls.get(0).getName());
    assertEquals("127.0.0.1", urls.get(0).getHost());
    assertEquals("/", urls.get(0).getPath());
  }

  @Test
  public void verifyThatURLSCanBeLoadedFromDeployModeOnKubernetes() throws IOException{
    URL url = JsonParserTest.class.getResource("/describe-component-deploy-kubernetes.json");
    JSonParser parser = new JSonParser(MAPPER.readTree(url));
    List< org.jboss.tools.intellij.openshift.utils.odo.URL> urls = parser.parseURLS();
    assertNotNull(urls);
    assertEquals(2, urls.size());
    assertEquals("80", urls.get(0).getLocalPort());
    assertEquals("80", urls.get(0).getContainerPort());
    assertEquals("my-nodejs-app", urls.get(0).getName());
    assertEquals("nodejs.example.com", urls.get(0).getHost());
    assertEquals("/", urls.get(0).getPath());
    assertEquals("80", urls.get(1).getLocalPort());
    assertEquals("80", urls.get(1).getContainerPort());
    assertEquals("my-nodejs-app", urls.get(1).getName());
    assertEquals("nodejs.example.com", urls.get(1).getHost());
    assertEquals("/health", urls.get(1).getPath());
  }

  @Test
  public void verifyThatURLSCanBeLoadedFromDeployModeOnOpenShift() throws IOException{
    URL url = JsonParserTest.class.getResource("/describe-component-deploy-openshift.json");
    JSonParser parser = new JSonParser(MAPPER.readTree(url));
    List< org.jboss.tools.intellij.openshift.utils.odo.URL> urls = parser.parseURLS();
    assertNotNull(urls);
    assertEquals(1, urls.size());
    assertEquals("80", urls.get(0).getLocalPort());
    assertEquals("80", urls.get(0).getContainerPort());
    assertEquals("my-nodejs-app", urls.get(0).getName());
    assertEquals("my-nodejs-app-jmaury-dev.apps.sandbox.x8i5.p1.openshiftapps.com", urls.get(0).getHost());
    assertEquals("/", urls.get(0).getPath());
  }
}
