package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnAction;
import org.jboss.tools.intellij.openshift.actions.ActionTest;
import org.jboss.tools.intellij.openshift.actions.service.LinkComponentAction;

public class LinkServiceActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new LinkServiceAction();
  }

  @Override
  protected void verifyComponent(boolean visible) {
    assertTrue(visible);
  }
}
