package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.ui.treeStructure.Tree;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.PersistentVolumeClaimNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ActionTest extends LightPlatformCodeInsightFixtureTestCase {
  public AnActionEvent createEvent(Object selected) {
    AnActionEvent event = mock(AnActionEvent.class);
    Presentation presentation = new Presentation();
    TreeSelectionModel model = mock(TreeSelectionModel.class);
    Tree tree = mock(Tree.class);
    TreePath path = mock(TreePath.class);
    when(path.getLastPathComponent()).thenReturn(selected);
    when(tree.getSelectionModel()).thenReturn(model);
    when(model.getSelectionPath()).thenReturn(path);
    when(event.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
    when(event.getPresentation()).thenReturn(presentation);
    return event;
  }

  public abstract AnAction getAction();

  public void testActionOnLoggedInCluster() {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    when(applicationsRootNode.isLogged()).thenReturn(true);
    AnActionEvent event = createEvent(applicationsRootNode);
    AnAction action = getAction();
    action.update(event);
    verifyLoggedInCluster(event.getPresentation().isVisible());
  }

  protected void verifyLoggedInCluster(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnLoggedOutCluster() {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    when(applicationsRootNode.isLogged()).thenReturn(false);
    AnActionEvent event = createEvent(applicationsRootNode);
    AnAction action = getAction();
    action.update(event);
    verifyLoggedOutCluster(event.getPresentation().isVisible());
  }

  protected void verifyLoggedOutCluster(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnProject() {
    ProjectNode projectNode = mock(ProjectNode.class);
    AnActionEvent event = createEvent(projectNode);
    AnAction action = getAction();
    action.update(event);
    verifyProject(event.getPresentation().isVisible());
  }

  protected void verifyProject(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnApplication() {
    ApplicationNode applicationNode = mock(ApplicationNode.class);
    AnActionEvent event = createEvent(applicationNode);
    AnAction action = getAction();
    action.update(event);
    verifyApplication(event.getPresentation().isVisible());
  }

  protected void verifyApplication(boolean visible) {
    assertFalse(visible);
  }

  public void testActionIsDisabledOnComponent() {
    ComponentNode componentNode = mock(ComponentNode.class);
    AnActionEvent event = createEvent(componentNode);
    AnAction action = getAction();
    action.update(event);
    verifyComponent(event.getPresentation().isVisible());
  }

  protected void verifyComponent(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnService() {
    ServiceNode serviceNode = mock(ServiceNode.class);
    AnActionEvent event = createEvent(serviceNode);
    AnAction action = getAction();
    action.update(event);
    verifyService(event.getPresentation().isVisible());
  }

  protected void verifyService(boolean visible) {
    assertFalse(visible);
  }


  public void testActionOnStorage() {
    PersistentVolumeClaimNode storageNode = mock(PersistentVolumeClaimNode.class);
    AnActionEvent event = createEvent(storageNode);
    AnAction action = getAction();
    action.update(event);
    verifyStorage(event.getPresentation().isVisible());
  }

  protected void verifyStorage(boolean visible) {
    assertFalse(visible);
  }


}
