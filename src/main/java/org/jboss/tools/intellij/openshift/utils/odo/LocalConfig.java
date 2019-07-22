package org.jboss.tools.intellij.openshift.utils.odo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalConfig {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Storage {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Size")
        private String size;

        @JsonProperty("Path")
        private String path;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Env {
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Value")
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComponentSettings {
        @JsonProperty("Type")
        private String type;

        @JsonProperty("SourceLocation")
        private String sourceLocation;

        @JsonProperty("SourceType")
        private String sourceType;

        @JsonProperty("Application")
        private String application;

        @JsonProperty("Project")
        private String project;

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Storage")
        private List<Storage> storages;

        @JsonProperty("Envs")
        private List<Env> envs;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSourceLocation() {
            return sourceLocation;
        }

        public void setSourceLocation(String sourceLocation) {
            this.sourceLocation = sourceLocation;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Storage> getStorages() {
            return storages;
        }

        public void setStorages(List<Storage> storages) {
            this.storages = storages;
        }

        public List<Env> getEnvs() {
            return envs;
        }

        public void setEnvs(List<Env> envs) {
            this.envs = envs;
        }
    }

    @JsonProperty("ComponentSettings")
    private ComponentSettings componentSettings;

    public ComponentSettings getComponentSettings() {
        return componentSettings;
    }

    public void setComponentSettings(ComponentSettings componentSettings) {
        this.componentSettings = componentSettings;
    }

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static LocalConfig load(URL url) throws IOException {
        return mapper.readValue(url, LocalConfig.class);
    }
}
