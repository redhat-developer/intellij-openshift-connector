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
import java.util.List;

public class ComponentDescriptorsDeserializer extends StdNodeBasedDeserializer<List<ComponentDescriptor>> {

    public static final String COMPONENT_IN_DEVFILE_FIELD = "componentInDevfile";
    private final String path;

    public ComponentDescriptorsDeserializer(String path) {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, ComponentDescriptor.class));
        this.path = path;
    }

    @Override
    public List<ComponentDescriptor> convert(JsonNode root, DeserializationContext context) {
        List<ComponentDescriptor> result = new ArrayList<>();
        if (root.has(COMPONENT_IN_DEVFILE_FIELD)) {
            result.add(new ComponentDescriptor(root.get(COMPONENT_IN_DEVFILE_FIELD).asText(), path));
        }
        return result;
    }
}
