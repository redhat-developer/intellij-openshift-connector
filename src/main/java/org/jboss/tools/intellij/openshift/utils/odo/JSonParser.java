package org.jboss.tools.intellij.openshift.utils.odo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class JSonParser {
    private static final String ITEMS_FIELD = "items";
    private static final String METADATA_FIELD = "metadata";
    private static final String NAME_FIELD = "name";
    private static final String SPEC_FIELD = "spec";
    private static final String SOURCE_TYPE_FIELD = "sourceType";
    private static final String TYPE_FIELD = "type";

    private final JsonNode root;

    public JSonParser(JsonNode root) {
        this.root = root;
    }

    public List<URL> parseURLS() {
        List<URL> result = new ArrayList<>();
        if (root.has(ITEMS_FIELD)) {
            root.get(ITEMS_FIELD).forEach(item -> {
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
        }
        return result;
    }

    public List<Application> parseApplications() {
        List<Application> result = new ArrayList<>();
        if (root.has(ITEMS_FIELD)) {
            root.get(ITEMS_FIELD).forEach(item -> result.add(Application.of(item.get(METADATA_FIELD).get(NAME_FIELD).asText())));
        }
        return result;
    }

    public ComponentInfo parseComponent() {
        if (root.has(SPEC_FIELD) && root.get(SPEC_FIELD).has(SOURCE_TYPE_FIELD)) {
            String sourceType = root.get(SPEC_FIELD).get(SOURCE_TYPE_FIELD).asText();
            String componentTypeName = root.get(SPEC_FIELD).get(TYPE_FIELD).asText();
            ComponentInfo.Builder builder = new ComponentInfo.Builder().withSourceType(ComponentSourceType.fromAnnotation(sourceType)).withComponentTypeName(componentTypeName);
            return builder.build();
            /*
            ComponentInfo.Builder builder = new ComponentInfo.Builder().withSourceType(sourceType).withComponentTypeName(deploymentConfig.getMetadata().getLabels().get(RUNTIME_NAME_LABEL)).withComponentTypeVersion(deploymentConfig.getMetadata().getLabels().get(RUNTIME_VERSION_LABEL)).withMigrated(deploymentConfig.getMetadata().getLabels().containsKey(ODO_MIGRATED_LABEL));
      if (sourceType == ComponentSourceType.LOCAL) {
        return builder.build();
      } else if (sourceType == ComponentSourceType.BINARY) {
        return builder.withBinaryURL(deploymentConfig.getMetadata().getAnnotations().get(VCS_URI_ANNOTATION)).build();
      } else {
        BuildConfig buildConfig = client.buildConfigs().inNamespace(project).withName(deploymentConfig.getMetadata().getName()).get();
        return builder.withRepositoryURL(deploymentConfig.getMetadata().getAnnotations().get(VCS_URI_ANNOTATION)).withRepositoryReference(buildConfig.getSpec().getSource().getGit().getRef()).build();
      }
             */
        }
        return new ComponentInfo.Builder().withSourceType(ComponentSourceType.LOCAL).build();
    }
}
