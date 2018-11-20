package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.actions.application.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LogoutAction extends OdoAction {
  public LogoutAction() {
    super(ApplicationsRootNode.class);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, OdoHelper odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
      CompletableFuture.runAsync(() -> {
        try {
          odo.logout();
          clusterNode.setLogged(false);
          clusterNode.reload();
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Logout", JOptionPane.ERROR_MESSAGE));
        }
      });
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    Component comp = getTree(e);
    if (comp instanceof Tree) {
      TreePath selectPath = ((Tree) comp).getSelectionModel().getSelectionPath();
      Object selected = selectPath.getLastPathComponent();
      if (selected instanceof ApplicationsRootNode) {
        e.getPresentation().setVisible(((ApplicationsRootNode)selected).isLogged());
      }
    }


  }
}
