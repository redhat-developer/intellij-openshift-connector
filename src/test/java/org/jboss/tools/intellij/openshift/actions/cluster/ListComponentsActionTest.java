package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnAction;

public class ListComponentsActionTest extends LogoutActionTest {
  @Override
  public AnAction getAction() {
    return new ListComponentsAction();
  }
}
