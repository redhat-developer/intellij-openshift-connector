/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistriesNode;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistryNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.PersistentVolumeClaimNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.tree.application.URLNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentState;
import org.jboss.tools.intellij.openshift.utils.odo.URL;

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
    when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
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
    NamespaceNode projectNode = mock(NamespaceNode.class);
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

  private AnActionEvent setupActionOnComponent(Component component) {
    ComponentNode componentNode = mock(ComponentNode.class);
    when(componentNode.getComponent()).thenReturn(component);
    AnActionEvent event = createEvent(componentNode);
    AnAction action = getAction();
    action.update(event);
    return event;
  }

  private AnActionEvent setupActionOnURL(URL url) {
    URLNode urlNode = mock(URLNode.class);
    when(urlNode.getUrl()).thenReturn(url);
    AnActionEvent event = createEvent(urlNode);
    AnAction action = getAction();
    action.update(event);
    return event;
  }

  private ComponentInfo mockInfo(){
    ComponentInfo.Builder builder = new ComponentInfo.Builder();
    return builder.withComponentKind(ComponentKind.S2I).build();
  }

  public void testActionOnPushedComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", ComponentState.PUSHED, ".", mockInfo()));
    verifyPushedComponent(event.getPresentation().isVisible());
  }

  protected void verifyPushedComponent(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnNotPushedComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", ComponentState.NOT_PUSHED, ".", mockInfo()));
    verifyNotPushedComponent(event.getPresentation().isVisible());
  }

  protected void verifyNotPushedComponent(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnNoContextComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", ComponentState.NO_CONTEXT, mockInfo()));
    verifyNoContextComponent(event.getPresentation().isVisible());
  }

  protected void verifyNoContextComponent(boolean visible) {
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

  protected void verifyNotPushedURL(boolean visible) {
    assertFalse(visible);
  }

  protected void verifyPushedURL(boolean visible) {
    assertFalse(visible);
  }

  protected void verifyLocallyDeletedURL(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnNotPushedURL() {
    AnActionEvent event = setupActionOnURL(URL.of("url1", "https", "localhost", "8080", "Not Pushed", false));
    AnAction action = getAction();
    action.update(event);
    verifyNotPushedURL(event.getPresentation().isVisible());
  }

  public void testActionOnPushedURL() {
    AnActionEvent event = setupActionOnURL(URL.of("url1", "https", "localhost", "8080", "Pushed", false));
    AnAction action = getAction();
    action.update(event);
    verifyPushedURL(event.getPresentation().isVisible());
  }

  public void testActionOnLocallyDeleteURL() {
    AnActionEvent event = setupActionOnURL(URL.of("url1", "https", "localhost", "8080", "Locally Deleted", false));
    AnAction action = getAction();
    action.update(event);
    verifyLocallyDeletedURL(event.getPresentation().isVisible());
  }

  protected void verifyStorage(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnRegistries() {
    DevfileRegistriesNode registriesNode = mock(DevfileRegistriesNode.class);
    AnActionEvent event = createEvent(registriesNode);
    AnAction action = getAction();
    action.update(event);
    verifyRegistries(event.getPresentation().isVisible());
  }

  protected void verifyRegistries(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnRegistry() {
    DevfileRegistryNode registryNode = mock(DevfileRegistryNode.class);
    AnActionEvent event = createEvent(registryNode);
    AnAction action = getAction();
    action.update(event);
    verifyRegistry(event.getPresentation().isVisible());
  }

  protected void verifyRegistry(boolean visible) {
    assertFalse(visible);
  }
}
