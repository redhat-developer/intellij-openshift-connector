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
    public static final String NAME_FIELD = "name";
    private static final String MANAGED_BY_FIELD = "managedBy";

    public ComponentDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Component.class));
    }

    @Override
    public List<Component> convert(JsonNode root, DeserializationContext context) {
        return new ArrayList<>(parseComponents(root.get(COMPONENTS_FIELD)));
    }

    private Collection<Component> parseComponents(JsonNode tree) {
        List<Component> result = new ArrayList<>();
        if (tree != null) {
            for (JsonNode item : tree) {
                result.add(Component.of(getName(item), getManagedBy(item), getComponentState(item), getComponentInfo(item)));
            }
        }
        return result;
    }

    private ComponentFeatures getComponentState(JsonNode item) {
        JSonParser parser = new JSonParser(item);
        return parser.parseComponentState();
    }

    private ComponentInfo getComponentInfo(JsonNode item) {
        JSonParser parser = new JSonParser(item);
        return parser.parseComponentInfo();
    }

    private String getName(JsonNode item) {
        return JSonParser.getAsText(item, NAME_FIELD, "");
    }

    private String getManagedBy(JsonNode item) {
        return JSonParser.getAsText(item, MANAGED_BY_FIELD, "");
    }

}
