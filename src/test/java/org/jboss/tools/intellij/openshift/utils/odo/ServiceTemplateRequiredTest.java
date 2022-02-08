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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ServiceTemplateRequiredTest {
  private static final URL url = ServiceTemplateRequiredTest.class.getResource("/service-template-required-test.json");

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(List.class, new ServiceTemplatesDeserializer(s -> null));
    MAPPER.registerModule(module);
  }

  @Test
  public void verifyThatServiceTemplatesCanLoad() throws IOException {
    List<ServiceTemplate> serviceTemplates = MAPPER.readValue(url, new TypeReference<List<ServiceTemplate>>() {});
    Assert.assertNotNull(serviceTemplates);
  }

  @Test
  public void verifyThatServiceTemplatesReturnsItems() throws IOException {
    List<ServiceTemplate> serviceTemplates = MAPPER.readValue(url, new TypeReference<List<ServiceTemplate>>() {});
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(7, serviceTemplates.size());
    Assert.assertNotNull(serviceTemplates.get(6));
  }

  @Test
  public void verifyThatServiceTemplatesReturnsName() throws IOException {
    List<ServiceTemplate> serviceTemplates = MAPPER.readValue(url, new TypeReference<List<ServiceTemplate>>() {});
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(7, serviceTemplates.size());
    ServiceTemplate serviceTemplate = serviceTemplates.get(6);
    Assert.assertNotNull(serviceTemplate);
    Assert.assertEquals("web-terminal.v1.4.0", serviceTemplate.getName());
  }

  @Test
  public void verifyThatServiceTemplatesReturnsCRDs() throws IOException {
    List<ServiceTemplate> serviceTemplates = MAPPER.readValue(url, new TypeReference<List<ServiceTemplate>>() {});
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(7, serviceTemplates.size());
    ServiceTemplate serviceTemplate = serviceTemplates.get(6);
    Assert.assertNotNull(serviceTemplate);
    Assert.assertEquals("web-terminal.v1.4.0", serviceTemplate.getName());
    assertTrue(serviceTemplate instanceof ServiceTemplate);
    ServiceTemplate operatorServiceTemplate = (ServiceTemplate) serviceTemplate;
    assertNotNull(operatorServiceTemplate.getCRDs());
    assertEquals(2, operatorServiceTemplate.getCRDs().size());
  }

  @Test
  public void verifyThatServiceTemplatesReturnsCRDInfo() throws IOException {
    List<ServiceTemplate> serviceTemplates = MAPPER.readValue(url, new TypeReference<List<ServiceTemplate>>() {});
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(7, serviceTemplates.size());
    ServiceTemplate serviceTemplate = serviceTemplates.get(6);
    Assert.assertNotNull(serviceTemplate);
    Assert.assertEquals("web-terminal.v1.4.0", serviceTemplate.getName());
    assertTrue(serviceTemplate instanceof ServiceTemplate);
    ServiceTemplate operatorServiceTemplate = (ServiceTemplate) serviceTemplate;
    assertNotNull(operatorServiceTemplate.getCRDs());
    assertEquals(2, operatorServiceTemplate.getCRDs().size());
    OperatorCRD crd = operatorServiceTemplate.getCRDs().get(0);
    assertEquals("devworkspaceroutings.controller.devfile.io", crd.getName());
    assertEquals("v1alpha1", crd.getVersion());
    assertEquals("DevWorkspaceRouting", crd.getKind());
    assertEquals("devworkspaceroutings.controller.devfile.io", crd.getDisplayName());
    assertEquals("devworkspaceroutings.controller.devfile.io", crd.getDescription());
    assertNull(crd.getSample());
    assertNull(crd.getSchema());
    assertNotNull(crd.getSpecDescriptors());
    assertEquals(0, crd.getSpecDescriptors().size());
  }
}
