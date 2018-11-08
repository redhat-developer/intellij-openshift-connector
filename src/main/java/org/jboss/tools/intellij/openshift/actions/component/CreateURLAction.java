package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.*;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CreateURLAction extends OdoAction {
  public CreateURLAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    ProjectNode projectNode = (ProjectNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        ExecHelper.execute(odo, "app", "set", applicationNode.toString());
        ExecHelper.execute(odo, "component", "set", componentNode.toString());
        List<Integer> ports = loadServicePorts(componentNode, projectNode);
        if (!ports.isEmpty()) {
          Integer port;
          if (ports.size() > 1) {
            port = (Integer)UIHelper.executeinUI(() -> JOptionPane.showInputDialog(null, "Service port", "Choose port", JOptionPane.QUESTION_MESSAGE, null, ports.toArray(), ports.get(0)));
          } else {
            port = ports.get(0);
          }
          if (port != null) {
            ExecHelper.execute(odo, "url", "create", "--port", port.toString());
          }
        } else {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Can't create url for component without ports", "Create URL", JOptionPane.ERROR_MESSAGE));
        }
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Create URL", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  public List<Integer> loadServicePorts(TreeNode componentNode, ProjectNode projectNode) {
    final OpenShiftClient client = ((ApplicationsRootNode)((DefaultMutableTreeNode)componentNode).getRoot()).getClient();
    Service service = client.services().inNamespace(projectNode.toString()).withName(componentNode.toString() + '-' + componentNode.getParent().toString()).get();
    return service.getSpec().getPorts().stream().map(ServicePort::getPort).collect(Collectors.toList());
  }
}
