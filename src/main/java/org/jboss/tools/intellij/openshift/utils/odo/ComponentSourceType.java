package org.jboss.tools.intellij.openshift.utils.odo;

public enum ComponentSourceType {
    LOCAL("Local", "local"),
    GIT("Git", "git"),
    BINARY("Binary", "binary");

    private final String label;
    private final String annotation;

    ComponentSourceType(String label, String annotation) {
        this.label = label;
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return label;
    }

    public static ComponentSourceType fromAnnotation(String annotation) {
        for(ComponentSourceType sourceType : values()) {
            if (sourceType.annotation.equals(annotation)) {
                return sourceType;
            }
        }
        return LOCAL;
    }
}
