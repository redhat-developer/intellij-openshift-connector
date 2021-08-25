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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class ServiceTest {
  private static final URL url = ServiceTest.class.getResource("/service-test.json");

  private static ObjectMapper MAPPER;

  @BeforeClass
  public static void setup() {
    MAPPER = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(List.class, new ServiceDeserializer());
    MAPPER.registerModule(module);
  }

  @Test
  public void verifyThatServicesCanLoad() throws IOException {
    List<Service> services = MAPPER.readValue(url, new TypeReference<List<Service>>() {});
    Assert.assertNotNull(services);
  }

  @Test
  public void verifyThatServicesReturnsItems() throws IOException {
    List<Service> services = MAPPER.readValue(url, new TypeReference<List<Service>>() {});
    Assert.assertNotNull(services);
    Assert.assertEquals(4, services.size());
    Assert.assertNotNull(services.get(0));
  }

  @Test
  public void verifyThatServicesReturnsName() throws IOException {
    List<Service> services = MAPPER.readValue(url, new TypeReference<List<Service>>() {});
    Assert.assertNotNull(services);
    Assert.assertEquals(4, services.size());
    Service service = services.get(0);
    Assert.assertNotNull(service);
    Assert.assertEquals("my-cluster", service.getName());
  }
}
