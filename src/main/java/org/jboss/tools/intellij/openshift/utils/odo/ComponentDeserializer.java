/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
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

public class ComponentDeserializer extends StdNodeBasedDeserializer<List<Component>> {

    public static final String COMPONENTS_FIELD = "components";
    public static final String OTHER_FIELD = "otherComponents";
    public static final String METADATA_FIELD = "metadata";
    public static final String NAME_FIELD = "name";

    public ComponentDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Component.class));
    }

    @Override
    public List<Component> convert(JsonNode root, DeserializationContext context) {
        List<Component> result = new ArrayList<>();
        result.addAll(parseComponents(root.get(COMPONENTS_FIELD), ComponentKind.DEVFILE));
        return result;
    }

    private Collection<Component> parseComponents(JsonNode tree, ComponentKind kind) {
        List<Component> result = new ArrayList<>();
        if (tree != null) {
            for (JsonNode item : tree) {
                result.add(Component.of(getName(item), getComponentState(item), getComponentInfo(item, kind)));
            }
        }
        return result;
    }

    private ComponentState getComponentState(JsonNode item) {
        JSonParser parser = new JSonParser(item);
        return parser.parseComponentState();

    }

    private ComponentInfo getComponentInfo(JsonNode item, ComponentKind kind) {
        JSonParser parser = new JSonParser(item);
        return parser.parseComponentInfo(kind);
    }

    private String getName(JsonNode item) {
        if (item.has(NAME_FIELD)) {
            return item.get(NAME_FIELD).asText();
        } else {
            return "";
        }
    }
}
