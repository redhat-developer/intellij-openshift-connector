package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.TreeAction;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import java.io.IOException;

public class OdoAction extends TreeAction {
  public OdoAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    try {
      this.actionPerformed(anActionEvent, path, selected, OdoHelper.get());
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, OdoHelper odo) {
  }
}
