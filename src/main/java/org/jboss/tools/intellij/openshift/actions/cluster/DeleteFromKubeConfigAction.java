package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.kubernetes.api.model.NamedContext;
import org.jboss.tools.intellij.openshift.actions.TreeAction;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.tree.ClustersTreeModel;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class DeleteFromKubeConfigAction extends TreeAction {
    public DeleteFromKubeConfigAction() {
        super(NamedContext.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        NamedContext context = (NamedContext) selected;
        ClustersTreeModel model = (ClustersTreeModel) getTree(anActionEvent).getModel();
        model.getConfig().getContexts().remove(context);
        try {
            ConfigHelper.saveKubeConfig(model.getConfig());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
