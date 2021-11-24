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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComponentDescriptorsDeserializer extends StdNodeBasedDeserializer<List<ComponentDescriptor>> {

    public static final String DEVFILE_FIELD = "devfileComponents";
    public static final String PORTS_FIELD = "ports";
    public static final String METADATA_FIELD = "metadata";
    public static final String SPEC_FIELD = "spec";
    public static final String APP_FIELD = "app";
    public static final String STATUS_FIELD = "status";
    public static final String CONTEXT_FIELD = "context";
    public static final String NAMESPACE_FIELD = "namespace";
    public static final String NAME_FIELD = "name";

    public ComponentDescriptorsDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, ComponentDescriptor.class));
    }

    @Override
    public List<ComponentDescriptor> convert(JsonNode root, DeserializationContext context) {
        List<ComponentDescriptor> result = new ArrayList<>();
        result.addAll(parseComponents(root.get(DEVFILE_FIELD)));
        return result;
    }

    private Collection<? extends ComponentDescriptor> parseComponents(JsonNode tree) {
        List<ComponentDescriptor> result = new ArrayList<>();
        if (tree != null) {
            for (JsonNode item : tree) {
                result.add(new ComponentDescriptor(getProject(item), getApplication(item), getPath(item), getName(item),
                        getPorts(item)));
            }
        }
        return result;
    }

    private List<Integer> getPorts(JsonNode item) {
        List<Integer> ports = new ArrayList<>();
        if (item.has(SPEC_FIELD) && item.get(SPEC_FIELD).has(PORTS_FIELD)) {
            for (JsonNode portNode : item.get(SPEC_FIELD).get(PORTS_FIELD)) {
                String port = portNode.asText();
                if (port.endsWith("/TCP")) {
                    ports.add(Integer.parseInt(port.substring(0, port.length() - 4)));
                } else {
                    ports.add(Integer.parseInt(port));
                }
            }
        }
        return ports;
    }

    private String getProject(JsonNode item) {
        if (item.has(METADATA_FIELD) && item.get(METADATA_FIELD).has(NAMESPACE_FIELD)) {
            return item.get(METADATA_FIELD).get(NAMESPACE_FIELD).asText();
        } else {
            return "";
        }
    }

    private String getApplication(JsonNode item) {
        if (item.has(ComponentDescriptorsDeserializer.SPEC_FIELD) && item.get(ComponentDescriptorsDeserializer.SPEC_FIELD).has(APP_FIELD)) {
            return item.get(ComponentDescriptorsDeserializer.SPEC_FIELD).get(APP_FIELD).asText();
        } else {
            return "";
        }
    }

    private String getPath(JsonNode item) {
        if (item.has(STATUS_FIELD) && item.get(STATUS_FIELD).has(CONTEXT_FIELD)) {
            return item.get(STATUS_FIELD).get(CONTEXT_FIELD).asText();
        } else {
            return "";
        }
    }

    private String getName(JsonNode item) {
        if (item.has(METADATA_FIELD) && item.get(METADATA_FIELD).has(NAME_FIELD)) {
            return item.get(METADATA_FIELD).get(NAME_FIELD).asText();
        } else {
            return "";
        }
    }
}
