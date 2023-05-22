package org.jboss.tools.intellij.openshift.actions.component;

import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;

public class DevOnPodmanComponentAction extends FeatureComponentAction {

    public DevOnPodmanComponentAction() {
        super(ComponentFeature.DEV_ON_PODMAN);
    }

    @Override
    protected String getActionName() {
        return ComponentFeature.DEV_ON_PODMAN.getLabel();
    }

    @Override
    protected String getTelemetryActionName() {
        return ComponentFeature.DEV_ON_PODMAN.getLabel() + " component";
    }

    @Override
    protected ComponentFeature getComponentFeature(Component component) {
        return ComponentFeature.DEV_ON_PODMAN;
    }
}
