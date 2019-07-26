package org.jboss.tools.intellij.openshift.utils.odo;

public enum ComponentState {
    PUSHED("\u25C9 pushed"),
    NOT_PUSHED("\u25CE not pushed"),
    NO_CONTEXT("\u2718 no context");

    private String label;

    ComponentState(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
