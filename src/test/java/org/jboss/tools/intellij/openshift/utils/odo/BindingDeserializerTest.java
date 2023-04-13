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
package org.jboss.tools.intellij.openshift.utils.odo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class BindingDeserializerTest {
  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(List.class, new BindingDeserializer());
    MAPPER.registerModule(module);
  }

  @Test
  public void verifyThatBindingsCanLoad() throws IOException {
    URL url = BindingDeserializerTest.class.getResource("/describe-binding-test.json");
    List<Binding> bindings = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(bindings);
  }

  @Test
  public void verifyThatBindingDeserializerReturnsSimpleBindings() throws IOException {
    URL url = BindingDeserializerTest.class.getResource("/describe-binding-test.json");
    List<Binding> bindings = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(bindings);
    Assert.assertEquals(2, bindings.size());
    Binding binding = bindings.get(0);
    Assert.assertNotNull(binding);
    Assert.assertEquals("my-nodejs-app-cluster-sample-k8s",binding.getName());
    Service service = binding.getService();
    Assert.assertNotNull(service);
    Assert.assertEquals("cluster-sample",service.getName());
    Assert.assertEquals("Cluster",service.getKind());
    Assert.assertEquals("postgresql.k8s.enterprisedb.io/v1",service.getApiVersion());
    Assert.assertTrue(binding.getEnvironmentVariables().isEmpty());
  }

  @Test
  public void verifyThatBindingDeserializerReturnsInDevBindings() throws IOException {
    URL url = BindingDeserializerTest.class.getResource("/describe-binding-inDev-test.json");
    List<Binding> bindings = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(bindings);
    Assert.assertEquals(2, bindings.size());
    Binding binding = bindings.get(0);
    Assert.assertNotNull(binding);
    Assert.assertEquals("my-nodejs-app-cluster-sample-k8s",binding.getName());
    Service service = binding.getService();
    Assert.assertNotNull(service);
    Assert.assertEquals("cluster-sample",service.getName());
    Assert.assertEquals("Cluster",service.getKind());
    Assert.assertEquals("postgresql.k8s.enterprisedb.io/v1",service.getApiVersion());
    List<String> envs = binding.getEnvironmentVariables();
    Assert.assertFalse(envs.isEmpty());
    Assert.assertEquals(1, envs.size());
    Assert.assertEquals("PASSWORD", envs.get(0));
  }

}
