package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnAction;
import org.jboss.tools.intellij.openshift.actions.ActionTest;

public class LinkComponentActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new LinkComponentAction();
  }

  @Override
  protected void verifyService(boolean visible) {
    assertTrue(visible);
  }
}
