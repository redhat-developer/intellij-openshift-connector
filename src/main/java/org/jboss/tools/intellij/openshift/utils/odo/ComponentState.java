package org.jboss.tools.intellij.openshift.utils.odo;

public enum ComponentState {
    PUSHED("pushed"),
    NOT_PUSHED("not pushed"),
    NO_CONTEXT("no context");

    private String label;

    ComponentState(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
