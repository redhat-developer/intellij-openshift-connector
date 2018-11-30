package org.jboss.tools.intellij.openshift.actions.cluster;

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
import static org.mockito.Mockito.when;

public class LoginActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new LoginAction();
  }

  @Override
  protected void verifyLoggedOutCluster(boolean visible) {
    assertTrue(visible);
  }
}
