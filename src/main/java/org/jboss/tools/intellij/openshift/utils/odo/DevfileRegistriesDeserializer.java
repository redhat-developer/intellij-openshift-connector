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

import java.util.ArrayList;
import java.util.List;

public class DevfileRegistriesDeserializer extends StdNodeBasedDeserializer<List<DevfileRegistry>> {
  private static final String NAME_FIELD = "name";
  
  private static final String URL_FIELD = "url";
  
  private static final String SECURE_FIELD = "secure";

  private static final String REGISTRIES_FIELD = "registries";

  public DevfileRegistriesDeserializer() {
    super(TypeFactory.defaultInstance().constructCollectionType(List.class, DevfileRegistry.class));
  }

  @Override
  public List<DevfileRegistry> convert(JsonNode root, DeserializationContext ctxt) {
    List<DevfileRegistry> result = new ArrayList<>();
    JsonNode registries = root.get(REGISTRIES_FIELD);
    if (registries != null) {
      for (JsonNode registry : registries) {
        result.add(getRegistry(registry));
      }
    }
    return result;
  }

  public static DevfileRegistry getRegistry(JsonNode registry) {
    return DevfileRegistry.of(registry.get(NAME_FIELD).asText(), registry.get(URL_FIELD).asText(),
        registry.get(SECURE_FIELD).asBoolean());
  }
}