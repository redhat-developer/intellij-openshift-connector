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
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.ui.treeStructure.Tree;
import java.util.concurrent.CompletableFuture;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.ChartReleaseNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistriesNode;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistryNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoriesNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoryNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.tree.application.URLNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeatures;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.Service;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ActionTest extends BasePlatformTestCase {
  public AnActionEvent createEvent(Object selected) {
    AnActionEvent event = mock(AnActionEvent.class);
    Presentation presentation = new Presentation();
    TreeSelectionModel model = mock(TreeSelectionModel.class);
    Tree tree = mock(Tree.class);
    TreePath path = mock(TreePath.class);
    ApplicationsTreeStructure structure = mock(ApplicationsTreeStructure.class);
    when(path.getLastPathComponent()).thenReturn(selected);
    when(tree.getSelectionModel()).thenReturn(model);
    when(model.getSelectionPath()).thenReturn(path);
    when(model.getSelectionPaths()).thenReturn(new TreePath[]{path});
    when(event.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
    when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(structure);
    if (selected instanceof ApplicationsRootNode) {
      when(structure.getApplicationsRoot()).thenReturn(selected);
    } else {
      CompletableFuture<Odo> odoFuture = createOdoFuture(false);
      ApplicationsRootNode applicationRootNode = createApplicationRootNode(odoFuture);
      when(structure.getApplicationsRoot()).thenReturn(applicationRootNode);
    }
    when(event.getPresentation()).thenReturn(presentation);
    return event;
  }

  public abstract AnAction getAction();

  public void testActionOnLoggedInOpenShiftCluster() {
    testActionOnLoggedInCluster(createOdoFuture(true));
  }

  public void testActionOnLoggedInKubeCluster() {
    testActionOnLoggedInCluster(createOdoFuture(false));
  }

  private void testActionOnLoggedInCluster(CompletableFuture<Odo> odoFuture) {
    ApplicationsRootNode applicationsRootNode = createApplicationRootNode(odoFuture);
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

  @NotNull
  protected static ApplicationsRootNode createApplicationRootNode(CompletableFuture<? extends Odo> odoFuture) {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    doReturn(true).when(applicationsRootNode).isLogged();
    doReturn(applicationsRootNode).when(applicationsRootNode).getRoot();
    doReturn(odoFuture).when(applicationsRootNode).getOdo();
    return applicationsRootNode;
  }

  @NotNull
  protected static CompletableFuture<Odo> createOdoFuture(boolean isOpenShift) {
    Odo odo = mock(Odo.class);
    when(odo.isOpenShift()).thenReturn(isOpenShift);

    CompletableFuture<Odo> odoFuture = mock(CompletableFuture.class);
    when(odoFuture.getNow(any())).thenReturn(odo);
    return odoFuture;
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

  private ComponentInfo mockInfo() {
    ComponentInfo.Builder builder = new ComponentInfo.Builder();
    return builder.build();
  }

  public void testActionOnLocalDevComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV),
      ".", mockInfo()));
    verifyLocalDevComponent(event.getPresentation().isVisible());
  }

  protected void verifyLocalDevComponent(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnLocalOnlyComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(), ".", mockInfo()));
    verifyLocalOnlyComponent(event.getPresentation().isVisible());
  }

  protected void verifyLocalOnlyComponent(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnRemoteOnlyDevComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV), "aPath", mockInfo()));
    verifyRemoteOnlyDevComponent(event.getPresentation().isVisible());
  }

  public void testActionOnRemoteOnlyDevComponentWithoutContext() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV), mockInfo()));
    verifyRemoteOnlyDevComponentWithoutContext(event.getPresentation().isVisible());
  }

  protected void verifyRemoteOnlyDevComponent(boolean visible) {
    assertFalse(visible);
  }

  protected void verifyRemoteOnlyDevComponentWithoutContext(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnService() {
    Service service = mock(Service.class);
    ServiceNode serviceNode = mock(ServiceNode.class);
    when(serviceNode.getService()).thenReturn(service);
    AnActionEvent event = createEvent(serviceNode);
    AnAction action = getAction();
    action.update(event);
    verifyService(event.getPresentation().isVisible());
  }

  protected void verifyService(boolean visible) {
    assertFalse(visible);
  }


  protected void verifyURL(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnURL() {
    AnActionEvent event = setupActionOnURL(URL.of("url1", "localhost", "8080", "8080"));
    AnAction action = getAction();
    action.update(event);
    verifyURL(event.getPresentation().isVisible());
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

  public void testActionOnChartRelease() {
    ChartReleaseNode chartRelease = mock(ChartReleaseNode.class);
    AnActionEvent event = createEvent(chartRelease);
    AnAction action = getAction();
    action.update(event);
    verifyChartRelease(event.getPresentation().isVisible());
  }

  protected void verifyChartRelease(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnHelmRepositories() {
    HelmRepositoriesNode helmRepositoriesNode = mock(HelmRepositoriesNode.class);
    AnActionEvent event = createEvent(helmRepositoriesNode);
    AnAction action = getAction();
    action.update(event);
    verifyHelmRepositories(event.getPresentation().isVisible());
  }

  protected void verifyHelmRepositories(boolean visible) {
    assertFalse(visible);
  }

  public void testActionOnHelmRepository() {
    HelmRepositoryNode helmRepositoryNode = mock(HelmRepositoryNode.class);
    AnActionEvent event = createEvent(helmRepositoryNode);
    AnAction action = getAction();
    action.update(event);
    verifyHelmRepository(event.getPresentation().isVisible());
  }

  protected void verifyHelmRepository(boolean visible) {
    assertFalse(visible);
  }
}
