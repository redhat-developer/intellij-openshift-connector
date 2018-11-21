package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.cluster.LoginDialog;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LoginAction extends LoggedOutClusterAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, OdoHelper odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    CompletableFuture.runAsync(() -> {
        try {
          LoginDialog loginDialog = UIHelper.executeInUI(() -> {
            LoginDialog dialog = new LoginDialog(null);
            dialog.show();
            return dialog;
            });
          if (loginDialog.isOK()) {
            odo.login(clusterNode.toString(), loginDialog.getUserName(), loginDialog.getPassword());
            clusterNode.setLogged(false);
            clusterNode.reload();
          }
        } catch (IOException e) {
          UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Login", JOptionPane.ERROR_MESSAGE));
        }
      });
  }
}
