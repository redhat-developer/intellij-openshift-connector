package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.jboss.tools.intellij.openshift.KubernetesLabels;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenInBrowserAction extends OdoAction {
  public OpenInBrowserAction() {
    super(ComponentNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
    ComponentNode componentNode = (ComponentNode) selected;
    ApplicationNode applicationNode = (ApplicationNode) ((TreeNode) selected).getParent();
    ProjectNode projectNode = (ProjectNode) applicationNode.getParent();
    CompletableFuture.runAsync(() -> {
      try {
        List<Route> routes = getRoute(projectNode, applicationNode, componentNode);
        if (routes.isEmpty()) {
          if (UIHelper.executeinUI(() -> JOptionPane.showConfirmDialog(null, "No URL for component " + componentNode.toString() + ", do you want to create one ?", "Create URL", JOptionPane.OK_CANCEL_OPTION)) == JOptionPane.OK_OPTION) {

          }
        } else {
          String url = getURL(routes.get(0));
          BrowserUtil.open(url);
        }
      } catch (KubernetesClientException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Open in Brower", JOptionPane.ERROR_MESSAGE));
      }
    });
  }

  protected LabelSelector getLabelSelector(ApplicationNode applicationNode, TreeNode componentNode) {
    return new LabelSelectorBuilder().addToMatchLabels(KubernetesLabels.APP_LABEL, applicationNode.toString())
      .addToMatchLabels(KubernetesLabels.COMPONENT_NAME_LABEL, componentNode.toString())
      .build();
  }
  public List<Route> getRoute(ProjectNode projectNode, ApplicationNode applicationNode, TreeNode componentNode) {
    final OpenShiftClient client = ((ApplicationsRootNode)((DefaultMutableTreeNode)componentNode).getRoot()).getClient();
    return client.routes().inNamespace(projectNode.toString()).withLabelSelector(getLabelSelector(applicationNode, componentNode)).list().getItems();
  }

  protected String getURL(Route route) {
    String hostname = route.getSpec().getHost();
    boolean isTls = route.getSpec().getTls() != null;
    return (isTls)? "https://" + hostname : "http://" + hostname;
  }
}
