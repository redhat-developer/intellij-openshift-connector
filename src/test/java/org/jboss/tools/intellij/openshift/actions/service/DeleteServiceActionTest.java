package org.jboss.tools.intellij.openshift.actions.service;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.actions.component.DeleteComponentAction;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.PersistentVolumeClaimNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.openshift.actions.ActionTest;

import static org.mockito.Mockito.mock;

public class DeleteServiceActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new DeleteServiceAction();
  }

  public void testThatActionIsDisabledOnCluster() {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    AnActionEvent event = createEvent(applicationsRootNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnProject() {
    ProjectNode projectNode = mock(ProjectNode.class);
    AnActionEvent event = createEvent(projectNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnApplication() {
    ApplicationNode applicationNode = mock(ApplicationNode.class);
    AnActionEvent event = createEvent(applicationNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnComponent() {
    ComponentNode componentNode = mock(ComponentNode.class);
    AnActionEvent event = createEvent(componentNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsEnabledOnService() {
    ServiceNode serviceNode = mock(ServiceNode.class);
    AnActionEvent event = createEvent(serviceNode);
    AnAction action = getAction();
    action.update(event);
    assertTrue(event.getPresentation().isVisible());
  }
  public void testThatActionIsDisabledOnStorage() {
    PersistentVolumeClaimNode storageNode = mock(PersistentVolumeClaimNode.class);
    AnActionEvent event = createEvent(storageNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }
}
