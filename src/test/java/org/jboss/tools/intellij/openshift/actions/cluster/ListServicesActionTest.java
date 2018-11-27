package org.jboss.tools.intellij.openshift.actions.cluster;

import com.intellij.openapi.actionSystem.AnAction;

public class ListServicesActionTest extends LogoutActionTest {
  @Override
  public AnAction getAction() {
    return new ListServicesAction();
  }
}
