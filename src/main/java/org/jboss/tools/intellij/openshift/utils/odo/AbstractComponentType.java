package org.jboss.tools.intellij.openshift.utils.odo;

public abstract class AbstractComponentType implements ComponentType {
    private String name;

    public AbstractComponentType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
