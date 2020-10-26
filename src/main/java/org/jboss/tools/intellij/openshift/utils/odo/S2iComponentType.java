package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.List;

public class S2iComponentType extends AbstractComponentType {


    private List<String> versions;

    public S2iComponentType(String name, List<String> versions){
        super(name);
        this.versions = versions;
    }

    public List<String> getVersions() {
        return versions;
    }

    @Override
    public ComponentKind getKind() {
        return ComponentKind.S2I;
    }
}
