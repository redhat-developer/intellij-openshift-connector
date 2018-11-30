package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.PersistentVolumeClaimNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.actions.ActionTest;

import static org.mockito.Mockito.mock;

public class DeleteComponentActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new DeleteComponentAction();
  }

  @Override
  protected void verifyComponent(boolean visible) {
    assertTrue(visible);
  }
}
