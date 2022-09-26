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

public class ComponentMetadatasDeserializer extends StdNodeBasedDeserializer<List<ComponentMetadata>> {

    private static final String DEVFILE_FIELD = "devfile";
    private static final String DEVFILE_REGISTRY_FIELD = "devfileRegistry";

    public ComponentMetadatasDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, ComponentMetadata.class));
    }

    @Override
    public List<ComponentMetadata> convert(JsonNode root, DeserializationContext ctxt){
        List<ComponentMetadata> result = new ArrayList<>();
        for(JsonNode item : root) {
            String componentType = get(item, DEVFILE_FIELD);
            String registry = get(item, DEVFILE_REGISTRY_FIELD);
            result.add(ComponentMetadata.of(registry, componentType));
        }
        return result;
    }
    private static String get(JsonNode node, String fieldName) {
        return node.has(fieldName)?node.get(fieldName).asText():"";
    }
}
