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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.jboss.tools.intellij.openshift.KubernetesLabels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceDeserializer extends StdNodeBasedDeserializer<List<Service>> {
  private static final String NAME_FIELD = "name";

  private static final String METADATA_FIELD = "metadata";
  private static final String LABELS_FIELD = "labels";
  private static final String KIND_FIELD = "kind";

  public ServiceDeserializer() {
    super(TypeFactory.defaultInstance().constructCollectionType(List.class, Service.class));
  }

  @Override
  public List<Service> convert(JsonNode root, DeserializationContext ctxt) throws IOException {
    List<Service> result = new ArrayList<>();
      for (JsonNode service : root) {
        result.add(getService(service));
    }
    return result;
  }

  public static Service getService(JsonNode service) {
    String componentName = service.get(METADATA_FIELD).has(LABELS_FIELD) && service.get(METADATA_FIELD).get(LABELS_FIELD).has(KubernetesLabels.COMPONENT_NAME_LABEL)?service.get(METADATA_FIELD).get(LABELS_FIELD).get(KubernetesLabels.COMPONENT_NAME_LABEL).asText():null;
    return Service.of(service.get(KIND_FIELD).asText() + '/' + service.get(METADATA_FIELD).get(NAME_FIELD).asText(), componentName);
  }
}