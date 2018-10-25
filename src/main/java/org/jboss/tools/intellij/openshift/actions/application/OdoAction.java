package org.jboss.tools.intellij.openshift.actions.application;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.TreeAction;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;
import org.jboss.tools.intellij.openshift.utils.ToolsConfig;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class OdoAction extends TreeAction {
  public OdoAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    OdoHelper.INSTANCE.getCommand().thenAccept(s -> this.actionPerformed(anActionEvent, path, selected, s));
  }

  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, String odo) {
  }
}
