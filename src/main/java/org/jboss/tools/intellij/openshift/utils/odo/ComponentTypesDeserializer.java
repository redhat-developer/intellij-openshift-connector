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

public class ComponentTypesDeserializer extends StdNodeBasedDeserializer<List<ComponentType>> {

    private static final String DEVFILE_REGISTRY = "registry";
    private static final String DEVFILE_DISPLAY_NAME_FIELD = "displayName";
    private static final String DEVFILE_DESCRIPTION_FIELD = "description";
    private static final String DEVFILE_NAME_FIELD = "name";

    public ComponentTypesDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, ComponentType.class));
    }

    @Override
    public List<ComponentType> convert(JsonNode root, DeserializationContext ctxt){
        return new ArrayList<>(parseDevfileItems(root));
    }
    private static String get(JsonNode node, String fieldName) {
        return node.has(fieldName)?node.get(fieldName).asText():"";
    }

    private Collection<? extends ComponentType> parseDevfileItems(JsonNode items) {
        List<ComponentType> result = new ArrayList<>();
        if (items != null) {
            for (JsonNode item : items) {
                String name = get(item, DEVFILE_NAME_FIELD);
                String displayName = get(item, DEVFILE_DISPLAY_NAME_FIELD);
                String description = get(item, DEVFILE_DESCRIPTION_FIELD);
                DevfileRegistry registry = DevfileRegistriesDeserializer.getRegistry(item.get(DEVFILE_REGISTRY));
                result.add(new DevfileComponentType(name, displayName, description, registry));
            }
        }
        return result;
    }
}
