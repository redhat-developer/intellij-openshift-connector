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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BindingDeserializer extends StdNodeBasedDeserializer<List<Binding>> {
  private static final String NAME_FIELD = "name";
  private static final String SPEC_FIELD = "spec";
  private static final String SERVICES_FIELD = "services";
  private static final String KIND_FIELD = "kind";
  private static final String APIVERSION_FIELD = "apiVersion";
  private static final String STATUS_FIELD = "status";
  private static final String BINDING_ENV_VARS_FIELD = "bindingEnvVars";

  public BindingDeserializer() {
    super(TypeFactory.defaultInstance().constructCollectionType(List.class, Binding.class));
  }

  @Override
  public List<Binding> convert(JsonNode root, DeserializationContext ctxt) {
    List<Binding> result = new ArrayList<>();
      for (JsonNode binding : root) {
        result.add(getBinding(binding));
      }
    return result;
  }

  public static Binding getBinding(JsonNode binding) {
    String name = binding.get(NAME_FIELD).asText();
    if (binding.has(SPEC_FIELD) && binding.get(SPEC_FIELD).has(SERVICES_FIELD)) {
      JsonNode serviceNode = binding.get(SPEC_FIELD).get(SERVICES_FIELD).get(0);
      if (serviceNode.has(KIND_FIELD) && serviceNode.has(NAME_FIELD) && serviceNode.has(APIVERSION_FIELD)) {
        Service service = Service.of(serviceNode.get(NAME_FIELD).asText(),
                serviceNode.get(APIVERSION_FIELD).asText(),
                serviceNode.get(KIND_FIELD).asText());
        List<String> bindingEnvVars = Collections.emptyList();
        if (binding.has(STATUS_FIELD) && binding.get(STATUS_FIELD).has(BINDING_ENV_VARS_FIELD)) {
          bindingEnvVars = new ArrayList<>();
          for(JsonNode bindingEnvVar : binding.get(STATUS_FIELD).get(BINDING_ENV_VARS_FIELD)) {
            bindingEnvVars.add(bindingEnvVar.asText());
          }
        }
        return Binding.of(name, service, bindingEnvVars);
      }
    }
    return null;
  }
}
