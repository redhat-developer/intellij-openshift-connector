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
import org.jboss.tools.intellij.openshift.utils.Serialization;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ComponentDeserializerTest {
  private static final URL url = ComponentDeserializerTest.class.getResource("/components-test.json");

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = Serialization.configure(new ComponentDeserializer());
  }

  @Test
  public void verifyThatComponentsCanLoad() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(components);
  }

  @Test
  public void verifyThatComponentDeserializerReturnsComponents() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(components);
    Assert.assertEquals(2, components.size());
    Assert.assertNotNull(components.get(0));
    Assert.assertNotNull(components.get(1));
  }

  @Test
  public void verifyThatComponentDeserializerReturnsComponentsPropertiesForDevfileComponent() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(components);
    Assert.assertEquals(2, components.size());
    //Devfile components
    Component component = components.get(0);
    Assert.assertNotNull(component);
    Assert.assertEquals("nodejs", component.getName());
    Assert.assertEquals("odo", component.getManagedBy());
    ComponentFeatures liveFeatures = component.getLiveFeatures();
    Assert.assertNotNull(liveFeatures);
    Assert.assertFalse(liveFeatures.isDebug());
    Assert.assertFalse(liveFeatures.isDeploy());
    Assert.assertTrue(liveFeatures.isDev());
    ComponentInfo info = component.getInfo();
    Assert.assertNotNull(info);
    Assert.assertEquals("nodejs", info.getComponentTypeName());
    Assert.assertEquals(ComponentKind.DEVFILE, info.getComponentKind());
    Assert.assertNull(info.getLanguage());
    List<ComponentFeature.Mode> features = info.getSupportedFeatures();
    Assert.assertNotNull(features);
    Assert.assertFalse(features.contains(ComponentFeature.Mode.DEBUG_MODE));
    Assert.assertFalse(features.contains(ComponentFeature.Mode.DEPLOY_MODE));
    Assert.assertFalse(features.contains(ComponentFeature.Mode.DEV_MODE));
  }

  @Test
  public void verifyThatComponentDeserializerReturnsComponentsPropertiesForNonOdoComponent() throws IOException {
    List<Component> components = MAPPER.readValue(url, new TypeReference<>() {});
    Assert.assertNotNull(components);
    Assert.assertEquals(2, components.size());

    //non odo components
    Component component = components.get(1);
    Assert.assertNotNull(component);
    Assert.assertEquals("nondevcomp", component.getName());
    ComponentFeatures liveFeatures = component.getLiveFeatures();
    Assert.assertNotNull(liveFeatures);
    Assert.assertFalse(liveFeatures.isDebug());
    Assert.assertFalse(liveFeatures.isDeploy());
    Assert.assertFalse(liveFeatures.isDev());
    ComponentInfo info = component.getInfo();
    Assert.assertNotNull(info);
    Assert.assertEquals("Unknown", info.getComponentTypeName());
    Assert.assertEquals(ComponentKind.DEVFILE, info.getComponentKind());
    Assert.assertNull(info.getLanguage());
    List<ComponentFeature.Mode> features = info.getSupportedFeatures();
    Assert.assertNotNull(features);
    Assert.assertFalse(features.contains(ComponentFeature.Mode.DEBUG_MODE));
    Assert.assertFalse(features.contains(ComponentFeature.Mode.DEPLOY_MODE));
    Assert.assertFalse(features.contains(ComponentFeature.Mode.DEV_MODE));
  }
}
