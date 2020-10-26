package org.jboss.tools.intellij.openshift.utils.odo;

public final class DevfileComponentType extends AbstractComponentType {

    public DevfileComponentType(String name) {
        super(name);
    }

    @Override
    public ComponentKind getKind() {
        return ComponentKind.DEVFILE;
    }

}
