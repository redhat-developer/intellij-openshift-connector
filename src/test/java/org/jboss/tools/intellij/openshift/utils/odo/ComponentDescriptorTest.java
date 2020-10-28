/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
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

public class ComponentDescriptorTest {
  private static final URL url = ComponentDescriptorTest.class.getResource("/component-descriptor-test.json");

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(List.class, new ComponentDescriptorsDeserializer());
    MAPPER.registerModule(module);
  }

  @Test
  public void verifyThatComponentDescriptorsCanLoad() throws IOException {
    List<ComponentDescriptor> descriptors = MAPPER.readValue(url, new TypeReference<List<ComponentDescriptor>>() {});
    Assert.assertNotNull(descriptors);
  }

  @Test
  public void verifyThatComponentDescriptorsReturnsComponentDescriptor() throws IOException {
    List<ComponentDescriptor> descriptors = MAPPER.readValue(url, new TypeReference<List<ComponentDescriptor>>() {});
    Assert.assertNotNull(descriptors);
    Assert.assertEquals(2, descriptors.size());
    Assert.assertNotNull(descriptors.get(0));
    Assert.assertNotNull(descriptors.get(1));
  }

  @Test
  public void verifyThatComponentDescriptorsReturnsComponentDescriptorProperties() throws IOException {
    List<ComponentDescriptor> descriptors = MAPPER.readValue(url, new TypeReference<List<ComponentDescriptor>>() {});
    Assert.assertNotNull(descriptors);
    Assert.assertEquals(2, descriptors.size());
    //S2iDescriptor
    ComponentDescriptor s2iDescriptor = descriptors.get(0);
    Assert.assertNotNull(s2iDescriptor);
    Assert.assertEquals("sbapp", s2iDescriptor.getApplication());
    Assert.assertEquals("testodo", s2iDescriptor.getProject());
    Assert.assertEquals("sbcomp", s2iDescriptor.getName());
    Assert.assertNotNull(s2iDescriptor.getPorts());
    List<Integer> ports = s2iDescriptor.getPorts();
    Assert.assertEquals(3, ports.size());
    Assert.assertEquals(8080, ports.get(0).intValue());
    Assert.assertEquals(8443, ports.get(1).intValue());
    Assert.assertEquals(8778, ports.get(2).intValue());
    //DevfileDescriptor
    ComponentDescriptor devfileDescriptor = descriptors.get(1);
    Assert.assertNotNull(devfileDescriptor);
    Assert.assertEquals("dev-app", devfileDescriptor.getApplication());
    Assert.assertEquals("devproj", devfileDescriptor.getProject());
    Assert.assertEquals("devcomp", devfileDescriptor.getName());

  }
}
