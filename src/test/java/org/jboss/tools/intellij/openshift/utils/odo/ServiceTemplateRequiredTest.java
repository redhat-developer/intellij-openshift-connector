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

public class ServiceTemplateRequiredTest {
  private static final URL url = ServiceTemplateRequiredTest.class.getResource("/service-template-required-test.json");

  private static ServiceTemplatesDeserializer DESERIALIZER;

  @BeforeClass
  public static void setup() {
    GenericKubernetesResource kafkaKind = new GenericKubernetesResource();
    kafkaKind.setApiVersion("rds.services.k8s.aws/v1alpha1");
    kafkaKind.setKind("DBCluster");
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
  }
}
