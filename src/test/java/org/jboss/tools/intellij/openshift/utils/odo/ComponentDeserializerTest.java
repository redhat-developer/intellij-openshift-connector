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
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ComponentDeserializerTest {
  private static final URL url = ComponentDeserializerTest.class.getResource("/components-test.json");

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(List.class, new ComponentDeserializer());
    MAPPER.registerModule(module);
  }

  @Test
  public void verifyThatComponentsCanLoad() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<List<Component>>() {});
    Assert.assertNotNull(components);
  }

  @Test
  public void verifyThatComponentDeserializerReturnsComponents() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<List<Component>>() {});
    Assert.assertNotNull(components);
    Assert.assertEquals(1, components.size());
    Assert.assertNotNull(components.get(0));
    //Assert.assertNotNull(components.get(1));
  }

  @Test
  public void verifyThatComponentDeserializerReturnsComponentsPropertiesForDevfileComponent() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<List<Component>>() {});
    Assert.assertNotNull(components);
    Assert.assertEquals(1, components.size());
    //Devfile components
    Component component = components.get(0);
    Assert.assertNotNull(component);
    Assert.assertEquals("nodejs1", component.getName());
  }

  @Test
  @Ignore
  public void verifyThatComponentDeserializerReturnsComponentsPropertiesForNonOdoComponent() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<List<Component>>() {});
    Assert.assertNotNull(components);
    Assert.assertEquals(2, components.size());
    //non odo components
    Component component = components.get(1);
    Assert.assertNotNull(component);
    Assert.assertEquals("quarkus1", component.getName());
  }
}
