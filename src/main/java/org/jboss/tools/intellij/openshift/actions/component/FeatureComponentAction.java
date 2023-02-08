/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public abstract class FeatureComponentAction extends ContextAwareComponentAction {

    protected final ComponentFeature feature;

    public FeatureComponentAction(ComponentFeature feature) {
        this.feature = feature;
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible && selected instanceof ComponentNode) {
            Component component = ((ComponentNode) selected).getComponent();
            visible = component.getInfo().getFeatures().is(feature);
        }
        return visible;
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Object node = adjust(getSelected(getTree(e)));
        if (node instanceof ComponentNode) {
            ComponentNode componentNode = ((ComponentNode) adjust(getSelected(getTree(e))));
            Component component = componentNode.getComponent();
            var feat = getComponentFeature(component);
            if (componentNode.getRoot().getOdo().isStarted(componentNode.getNamespace(), component.getPath(),
                    component.getName(), feat)) {
                e.getPresentation().setText("Stop " + feature.getLabel() + " mode");
            } else {
                e.getPresentation().setText("Start " + feature.getLabel() + " mode");
            }
        }
    }

    protected String getActionName() {
        return feature.getLabel();
    }

    @Override
    protected String getTelemetryActionName() {
        return feature.getLabel() + " component";
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Object selected, Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = componentNode.getComponent();
        NamespaceNode namespaceNode = componentNode.getParent();
        CompletableFuture.runAsync(() -> {
            try {
                var feat = getComponentFeature(component);
                process(odo, namespaceNode.getName(), component, feat, res -> {
                    if (component.getLiveFeatures().is(feat)) {
                        component.getLiveFeatures().removeFeature(feat);
                    } else {
                        component.getLiveFeatures().addFeature(feat);
                    }
                    ((ApplicationsTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(componentNode);
                });
                sendTelemetryResults(TelemetryResult.SUCCESS);
            } catch (IOException e) {
                sendTelemetryError(e);
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), getActionName()));
            }
        });
    }

    private ComponentFeature getComponentFeature(Component component) {
        return ((feature.getPeer() != null) && component.getInfo().getFeatures().is(feature.getPeer())) ?
                feature.getPeer() : feature;
    }

    protected void process(Odo odo, String project, Component component,
                           ComponentFeature feat, Consumer<Boolean> callback) throws IOException {
        if (odo.isStarted(project, component.getPath(), component.getName(), feat)) {
            odo.stop(project, component.getPath(), component.getName(), feat);
            callback.accept(true);
        } else {
            odo.start(project, component.getPath(), component.getName(), feat, callback);
        }
    }
}
