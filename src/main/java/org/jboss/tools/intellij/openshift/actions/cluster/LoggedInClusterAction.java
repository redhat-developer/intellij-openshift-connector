package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.actions.OdoAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;

import javax.swing.tree.TreePath;
import java.awt.Component;

public abstract class LoggedInClusterAction extends OdoAction {
  public LoggedInClusterAction() {
    super(ApplicationsRootNode.class);
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
