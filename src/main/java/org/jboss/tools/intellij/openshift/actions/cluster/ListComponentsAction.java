package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.UIHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ListComponentsAction extends LoggedInClusterAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    CompletableFuture.runAsync(() -> {
      try {
        odo.listComponents();
      } catch (IOException e) {
        UIHelper.executeInUI(() -> JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "List components", JOptionPane.ERROR_MESSAGE));
      }
    });
  }
}
