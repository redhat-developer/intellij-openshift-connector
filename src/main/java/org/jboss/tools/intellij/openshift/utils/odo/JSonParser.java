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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.Constants.DebugStatus;

import java.util.ArrayList;
import java.util.List;

public class JSonParser {
    private static final String ITEMS_FIELD = "items";
    private static final String METADATA_FIELD = "metadata";
    private static final String NAME_FIELD = "name";
    private static final String SPEC_FIELD = "spec";
    private static final String SOURCE_TYPE_FIELD = "sourceType";
    private static final String TYPE_FIELD = "type";
    private static final String URLS_FIELD = "urls";
    private static final String DEBUG_PROCESS_ID_FIELD = "debugProcessID";


    private final JsonNode root;

    public JSonParser(JsonNode root) {
        this.root = root;
    }

    public List<URL> parseURLS() {
        List<URL> result = new ArrayList<>();
        if (root.has(ITEMS_FIELD)) {
            result.addAll(parseURLItems(root.get(ITEMS_FIELD)));
        }
        if (root.has(SPEC_FIELD)) {
            result.addAll(parseURLItems(root.get(SPEC_FIELD).get(URLS_FIELD).get(ITEMS_FIELD)));
        }
        return result;
    }

    private List<URL> parseURLItems(JsonNode urlItems) {
        List<URL> result = new ArrayList<>();
        urlItems.forEach(item -> {
            //odo incorrectly reports urls created with the web ui without names
            if (item.get(METADATA_FIELD).has(NAME_FIELD)) {
                String name = item.get(METADATA_FIELD).get(NAME_FIELD).asText();
                String protocol = item.get(SPEC_FIELD).has("protocol") ?
                        item.get(SPEC_FIELD).get("protocol").asText() : "";
                String host = item.get(SPEC_FIELD).has("host") ?
                        item.get(SPEC_FIELD).get("host").asText() : "";
                String port = item.get(SPEC_FIELD).has("port") ? item.get(SPEC_FIELD).get("port").asText() : "0";
                result.add(URL.of(name, protocol, host, port, item.get("status").get("state").asText(), item.get(SPEC_FIELD).get("secure").asBoolean()));
            }
        });
        return result;
    }

    public List<Application> parseApplications() {
        List<Application> result = new ArrayList<>();
        if (root.has(ITEMS_FIELD)) {
            root.get(ITEMS_FIELD).forEach(item -> result.add(Application.of(item.get(METADATA_FIELD).get(NAME_FIELD).asText())));
        }
        return result;
    }

    public ComponentInfo parseComponentInfo(ComponentKind kind) {
        ComponentInfo.Builder builder = new ComponentInfo.Builder();
        if (root.has(SPEC_FIELD)) {
            String componentTypeName = root.get(SPEC_FIELD).get(TYPE_FIELD).asText();
            if (root.get(SPEC_FIELD).has(SOURCE_TYPE_FIELD)) {
                String sourceType = root.get(SPEC_FIELD).get(SOURCE_TYPE_FIELD).asText();
                builder.withSourceType(ComponentSourceType.fromAnnotation(sourceType)).withComponentTypeName(componentTypeName);
            } else {
                builder.withSourceType(ComponentSourceType.LOCAL).withComponentTypeName(componentTypeName);
            }
        }
        return builder.withComponentKind(kind).build();
    }

    public DebugStatus parseDebugStatus() {
        if (root.has(SPEC_FIELD)&& root.get(SPEC_FIELD).has(DEBUG_PROCESS_ID_FIELD)){
            String processID = root.get(SPEC_FIELD).get(DEBUG_PROCESS_ID_FIELD).asText();
            if (StringUtils.isNumeric(processID)){
                return DebugStatus.RUNNING;
            }
        }
        return Constants.DebugStatus.UNKNOWN;
    }
}
