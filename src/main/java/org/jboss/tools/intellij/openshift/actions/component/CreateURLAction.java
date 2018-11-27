package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.*;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreateURLAction extends OdoAction {
  public CreateURLAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    LazyMutableTreeNode applicationNode = (LazyMutableTreeNode) ((TreeNode) selected).getParent();
    LazyMutableTreeNode projectNode = (LazyMutableTreeNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<Integer> ports = loadServicePorts(odo, projectNode, applicationNode, componentNode);
        if (!ports.isEmpty()) {
          Integer port;
          if (ports.size() > 1) {
            port = (Integer)UIHelper.executeInUI(() -> JOptionPane.showInputDialog(null, "Service port", "Choose port", JOptionPane.QUESTION_MESSAGE, null, ports.toArray(), ports.get(0)));
          } else {
            port = ports.get(0);
          }
          if (port != null) {
            odo.createUrl(projectNode.toString(), applicationNode.toString(), componentNode.toString(), port);
          }
        } else {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Can't create url for component without ports", "Create URL", JOptionPane.ERROR_MESSAGE));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create URL", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  public List<Integer> loadServicePorts(Odo odo, LazyMutableTreeNode projectNode, LazyMutableTreeNode applicationNode, LazyMutableTreeNode componentNode) {
    final OpenShiftClient client = ((ApplicationsRootNode)componentNode.getRoot()).getClient();
    return odo.getServicePorts(client, projectNode.toString(), applicationNode.toString(), componentNode.toString());
  }
}
