package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.kubernetes.api.model.NamedContext;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeModel;

import java.io.IOException;

public class SetCurrentClusterAction extends TreeAction {
    public SetCurrentClusterAction() {
        super(NamedContext.class);
    }

    @Override
    void actionPerformed(AnActionEvent anActionEvent, Object selected) {
        NamedContext context = (NamedContext) selected;
        ClustersTreeModel model = (ClustersTreeModel) getTree(anActionEvent).getModel();
        model.getConfig().setCurrentContext(context.getName());
        try {
            ConfigHelper.saveKubeConfig(model.getConfig());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
