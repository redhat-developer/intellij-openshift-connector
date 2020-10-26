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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComponentDeserializer extends StdNodeBasedDeserializer<List<Component>> {

    public static final String S2I_FIELD = "s2iComponents";
    public static final String DEVFILE_FIELD = "devfileComponents";
    public static final String METADATA_FIELD = "metadata";
    public static final String STATUS_FIELD = "status";
    public static final String STATE_FIELD = "state";
    public static final String NAME_FIELD = "name";

    public ComponentDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Component.class));
    }

    @Override
    public List<Component> convert(JsonNode root, DeserializationContext context) throws IOException {
        List<Component> result = new ArrayList<>();
        // two roots, s2i and devfiles
        result.addAll(parseComponents(root.get(S2I_FIELD)));
        result.addAll(parseComponents(root.get(DEVFILE_FIELD)));
        return result;
    }

    private Collection<Component> parseComponents(JsonNode tree) throws IOException {
        List<Component> result = new ArrayList<>();
        if (tree != null) {
            for (JsonNode item : tree) {
                result.add(Component.of(getName(item), getState(item), getComponentInfo(item)));
            }
        }
        return result;
    }

    private ComponentInfo getComponentInfo(JsonNode item) throws IOException {
        JSonParser parser = new JSonParser(item);
        return parser.parseComponent();
    }

    private ComponentState getState(JsonNode item) {
        if (item.has(STATUS_FIELD) && item.get(STATUS_FIELD).has(STATE_FIELD)) {
            return ComponentState.fromState(item.get(STATUS_FIELD).get(STATE_FIELD).asText());
        } else {
            return ComponentState.NOT_PUSHED;
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
