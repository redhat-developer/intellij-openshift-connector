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

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.openshift.api.model.operatorhub.v1alpha1.ClusterServiceVersionList;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ServiceTemplateTest {
  private static final URL url = ServiceTemplateTest.class.getResource("/service-template-test.json");

  private static ServiceTemplatesDeserializer DESERIALIZER;

  @BeforeClass
  public static void setup() {
    GenericKubernetesResource kafkaKind = new GenericKubernetesResource();
    kafkaKind.setApiVersion("kafka.strimzi.io/v1beta2");
    kafkaKind.setKind("Kafka");
    DESERIALIZER = new ServiceTemplatesDeserializer(s -> null, Collections.singletonList(kafkaKind));
  }

  @Test
  public void verifyThatServiceTemplatesCanLoad() throws IOException {
    List<ServiceTemplate> serviceTemplates = DESERIALIZER.fromList(Serialization.unmarshal(url.openStream(), ClusterServiceVersionList.class));
    Assert.assertNotNull(serviceTemplates);
  }

  @Test
  public void verifyThatServiceTemplatesReturnsItems() throws IOException {
    List<ServiceTemplate> serviceTemplates = DESERIALIZER.fromList(Serialization.unmarshal(url.openStream(), ClusterServiceVersionList.class));
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(1, serviceTemplates.size());
    Assert.assertNotNull(serviceTemplates.get(0));
  }

  @Test
  public void verifyThatServiceTemplatesReturnsName() throws IOException {
    List<ServiceTemplate> serviceTemplates = DESERIALIZER.fromList(Serialization.unmarshal(url.openStream(), ClusterServiceVersionList.class));
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(1, serviceTemplates.size());
    ServiceTemplate serviceTemplate = serviceTemplates.get(0);
    Assert.assertNotNull(serviceTemplate);
    Assert.assertEquals("strimzi-cluster-operator.v0.32.0", serviceTemplate.getName());
  }

  @Test
  public void verifyThatServiceTemplatesReturnsCRDs() throws IOException {
    List<ServiceTemplate> serviceTemplates = DESERIALIZER.fromList(Serialization.unmarshal(url.openStream(), ClusterServiceVersionList.class));
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(1, serviceTemplates.size());
    ServiceTemplate serviceTemplate = serviceTemplates.get(0);
    Assert.assertNotNull(serviceTemplate);
    Assert.assertEquals("strimzi-cluster-operator.v0.32.0", serviceTemplate.getName());
    assertTrue(serviceTemplate instanceof ServiceTemplate);
    ServiceTemplate operatorServiceTemplate = (ServiceTemplate) serviceTemplate;
    assertNotNull(operatorServiceTemplate.getCRDs());
    assertEquals(1, operatorServiceTemplate.getCRDs().size());
  }

  @Test
  public void verifyThatServiceTemplatesReturnsCRDInfo() throws IOException {
    List<ServiceTemplate> serviceTemplates = DESERIALIZER.fromList(Serialization.unmarshal(url.openStream(), ClusterServiceVersionList.class));
    Assert.assertNotNull(serviceTemplates);
    Assert.assertEquals(1, serviceTemplates.size());
    ServiceTemplate serviceTemplate = serviceTemplates.get(0);
    Assert.assertNotNull(serviceTemplate);
    Assert.assertEquals("strimzi-cluster-operator.v0.32.0", serviceTemplate.getName());
    assertTrue(serviceTemplate instanceof ServiceTemplate);
    ServiceTemplate operatorServiceTemplate = (ServiceTemplate) serviceTemplate;
    assertNotNull(operatorServiceTemplate.getCRDs());
    assertEquals(1, operatorServiceTemplate.getCRDs().size());
    OperatorCRD crd = operatorServiceTemplate.getCRDs().get(0);
    assertEquals("kafkas.kafka.strimzi.io", crd.getName());
    assertEquals("v1beta2", crd.getVersion());
    assertEquals("Kafka", crd.getKind());
    assertEquals("Kafka", crd.getDisplayName());
    assertEquals("Represents a Kafka cluster", crd.getDescription());
    assertNotNull(crd.getSample());
    assertNull(crd.getSchema());
    assertNotNull(crd.getSpecDescriptors());
    assertEquals(7, crd.getSpecDescriptors().size());
    OperatorCRDSpecDescriptor descriptor = crd.getSpecDescriptors().get(0);
    assertEquals("kafka.version", descriptor.getPath());
    assertEquals("Version", descriptor.getDisplayName());
    assertEquals("Kafka version", descriptor.getDescription());
    assertEquals(1, descriptor.getDescriptors().size());
    assertEquals("urn:alm:descriptor:com.tectonic.ui:text", descriptor.getDescriptors().get(0));
  }
}
