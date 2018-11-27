package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;

public class OpenConsoleAction extends LoggedInClusterAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    ApplicationsRootNode clusterNode = (ApplicationsRootNode) selected;
    BrowserUtil.open(clusterNode.toString());
  }
}
