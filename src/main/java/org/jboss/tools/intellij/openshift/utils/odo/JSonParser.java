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
import com.fasterxml.jackson.databind.node.ObjectNode;
import kotlinx.serialization.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.Constants.DebugStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSonParser {
    private static final String ITEMS_FIELD = "items";
    private static final String METADATA_FIELD = "metadata";
    private static final String NAME_FIELD = "name";
    private static final String SPEC_FIELD = "spec";
    private static final String PROJECT_TYPE_FIELD = "projectType";
    private static final String URLS_FIELD = "urls";
    private static final String DEBUG_PROCESS_ID_FIELD = "debugProcessID";
    private static final String DEVFILE_FIELD = "devfile";

    private static final String DEVFILE_DATA_FIELD = "devfileData";
    private static final String STARTER_PROJECTS_FIELD = "starterProjects";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String REGISTRY_NAME_FIELD = "RegistryName";

    private static final String PATHS_FIELD = "paths";
    private static final String POST_FIELD = "post";
    private static final String PARAMETERS_FIELD = "parameters";
    private static final String BODY_VALUE = "body";
    private static final String SCHEMA_FIELD = "schema";
    private static final String DOLLAR_REF_FIELD = "$ref";
    private static final String ODO_SETTINGS_FIELD = "OdoSettings";
    private static final String REGISTRY_LIST_FIELD = "RegistryList";
    private static final String NAME1_FIELD = "Name";
    private static final String URL_FIELD = "URL";
    private static final String SECURE_FIELD = "secure";


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
                result.add(URL.of(name, protocol, host, port, item.get("status").get("state").asText(), item.get(SPEC_FIELD).get(SECURE_FIELD).asBoolean()));
            }
        });
        return result;
    }

    public ComponentInfo parseComponentInfo(ComponentKind kind) {
        ComponentInfo.Builder builder = new ComponentInfo.Builder().withComponentKind(kind);
        if (root.has(PROJECT_TYPE_FIELD)) {
            String componentTypeName = root.get(PROJECT_TYPE_FIELD).asText();
            builder.withComponentTypeName(componentTypeName);
        }
        return builder.build();
    }

    public ComponentInfo parseDescribeComponentInfo(ComponentKind kind) {
        ComponentInfo.Builder builder = new ComponentInfo.Builder().withComponentKind(kind);
        if (root.has(DEVFILE_DATA_FIELD) && root.get(DEVFILE_DATA_FIELD).has(DEVFILE_FIELD) &&
                root.get(DEVFILE_DATA_FIELD).get(DEVFILE_FIELD).has(METADATA_FIELD) &&
                root.get(DEVFILE_DATA_FIELD).get(DEVFILE_FIELD).get(METADATA_FIELD).has(PROJECT_TYPE_FIELD)) {
            String componentTypeName = root.get(DEVFILE_DATA_FIELD).get(DEVFILE_FIELD).get(METADATA_FIELD).get(PROJECT_TYPE_FIELD).asText();
            builder.withComponentTypeName(componentTypeName);
        }
        return builder.build();
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

    public ComponentTypeInfo parseComponentTypeInfo() {
        ComponentTypeInfo.Builder builder = new ComponentTypeInfo.Builder();
        for (JsonNode element : root) {
                    if (element.has(NAME_FIELD)) {
                        builder.withName(element.get(NAME_FIELD).asText());
                    }
                    if (element.has(STARTER_PROJECTS_FIELD)) {
                        for (JsonNode starter : element.get(STARTER_PROJECTS_FIELD)) {
                            builder.withStarter(parseStarter(starter));
                        }
                    }
        }
        return builder.build();
    }

    public Starter parseStarter(JsonNode node) {
        String name = node.asText();
        Starter.Builder builder = new Starter.Builder().withName(name);
        return builder.build();
    }

    private JsonNode resolveRefs(JsonNode root, JsonNode node) throws IOException {
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String name = it.next();
            JsonNode child = node.get(name);
            if (child.has(DOLLAR_REF_FIELD)) {
                JsonNode replaced = resolve(root, child.get(DOLLAR_REF_FIELD).asText());
                ((ObjectNode)node).set(name, replaced);
            } else {
                resolveRefs(root, child);
            }
        }
        return node;
    }

    private JsonNode resolve(JsonNode root, String ref) throws IOException {
        JsonNode node = root;
        String[] ids = ref.split("/");
        for(String id : ids) {
            if (!"#".equals(id)) {
                if (node.has(id)) {
                    node = node.get(id);
                } else {
                    throw new IOException("Can't resolved reference '" + ref + "' element " + id + " not found");
                }
            }
        }
        return node;
    }

    public ObjectNode findSchema(String crd) throws IOException {
        if (root.has(PATHS_FIELD)) {
            JsonNode node = root.get(PATHS_FIELD).get(crd);
            if (node != null && node.has(POST_FIELD) && node.get(POST_FIELD).has(PARAMETERS_FIELD)) {
                for(JsonNode parameter : node.get(POST_FIELD).get(PARAMETERS_FIELD)) {
                    if (parameter.has(NAME_FIELD) && parameter.get(NAME_FIELD).asText().equals(BODY_VALUE) && parameter.has(SCHEMA_FIELD)) {
                        JsonNode schema = parameter.get(SCHEMA_FIELD);
                        if (schema.has(DOLLAR_REF_FIELD)) {
                            return (ObjectNode) resolveRefs(root, resolve(root, schema.get(DOLLAR_REF_FIELD).asText()));
                        } else {
                            return (ObjectNode) resolveRefs(root, schema);
                        }
                    }
                }
            }
        }
        throw new IOException("Invalid data, no 'paths' field");
    }

    public List<DevfileRegistry> parseRegistries() {
        List<DevfileRegistry> result = new ArrayList<>();
        if (root.has(ODO_SETTINGS_FIELD) && root.get(ODO_SETTINGS_FIELD).has(REGISTRY_LIST_FIELD) &&
                root.get(ODO_SETTINGS_FIELD).get(REGISTRY_LIST_FIELD).isArray()) {
            for(JsonNode item : root.get(ODO_SETTINGS_FIELD).get(REGISTRY_LIST_FIELD)) {
                String name = item.get(NAME1_FIELD).asText();
                String url = item.get(URL_FIELD).asText();
                boolean secure = item.get(SECURE_FIELD).asBoolean();
                result.add(DevfileRegistry.of(name, url, secure));
            }
        }
        return result;
    }
}
