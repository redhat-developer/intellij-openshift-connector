package org.jboss.tools.intellij.openshift.utils.odo;

public enum ComponentKind {
    S2I("s2i"),
    DEVFILE("devfile");

    private final String label;

    ComponentKind(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
