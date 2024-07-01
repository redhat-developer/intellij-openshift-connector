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
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationRootNodeOdo;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsTreeStructure;
import org.jboss.tools.intellij.openshift.tree.application.BindingNode;
import org.jboss.tools.intellij.openshift.tree.application.ChartReleaseNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistriesNode;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistryComponentTypeNode;
import org.jboss.tools.intellij.openshift.tree.application.DevfileRegistryNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoriesNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoryNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.tree.application.ParentableNode;
import org.jboss.tools.intellij.openshift.tree.application.ServiceNode;
import org.jboss.tools.intellij.openshift.tree.application.URLNode;
import org.jboss.tools.intellij.openshift.utils.odo.Binding;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeatures;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentInfo;
import org.jboss.tools.intellij.openshift.utils.odo.Service;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the presentation of actions on tree ( visibility, labels, ... )
 * execution tests are done in integration tests
 */

@RunWith(Parameterized.class)
public abstract class ActionTest extends BasePlatformTestCase {

  protected final boolean isOpenshift;

  public ActionTest(boolean isOpenshift) {
    this.isOpenshift = isOpenshift;
  }

  @Parameterized.Parameters(name = "Openshift Cluster? : {0}")
  public static List<Boolean> data() {
    return Arrays.asList(true, false);
  }

  @NotNull
  protected ApplicationsRootNode createApplicationRootNode(boolean isLogged) {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    CompletableFuture<ApplicationRootNodeOdo> odoFuture = createOdoFuture();
    when(applicationsRootNode.isLogged()).thenReturn(isLogged);
    when(applicationsRootNode.getRoot()).thenReturn(applicationsRootNode);
    when(applicationsRootNode.getOdo()).thenReturn(odoFuture);
    ApplicationRootNodeOdo odo = mock(ApplicationRootNodeOdo.class);
    when(odo.getNamespaceKind()).thenReturn(isOpenshift ? "Project" : "Namespace");
    when(odoFuture.getNow(null)).thenReturn(odo);
    return applicationsRootNode;
  }

  @NotNull
  protected CompletableFuture<ApplicationRootNodeOdo> createOdoFuture() {
    ApplicationRootNodeOdo odo = mock(ApplicationRootNodeOdo.class);
    when(odo.isOpenShift()).thenReturn(isOpenshift);
    CompletableFuture<ApplicationRootNodeOdo> odoFuture = mock(CompletableFuture.class);
    when(odoFuture.getNow(any())).thenReturn(odo);
    return odoFuture;
  }

  public AnActionEvent createEvent(ParentableNode<?> node) {
    AnActionEvent event = mock(AnActionEvent.class);
    Presentation presentation = new Presentation();
    TreeSelectionModel model = mock(TreeSelectionModel.class);
    Tree tree = mock(Tree.class);
    TreePath path = mock(TreePath.class);
    ApplicationsTreeStructure structure = mock(ApplicationsTreeStructure.class);
    when(path.getLastPathComponent()).thenReturn(node);
    when(tree.getSelectionModel()).thenReturn(model);
    when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(structure);
    when(model.getSelectionPath()).thenReturn(path);
    when(model.getSelectionPaths()).thenReturn(new TreePath[]{path});
    when(event.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
    if (node instanceof ApplicationsRootNode) {
      when(structure.getApplicationsRoot()).thenReturn(node);
      when(node.getRoot()).thenReturn((ApplicationsRootNode) node);
    } else {
      ApplicationsRootNode applicationRootNode = createApplicationRootNode(true);
      when(structure.getApplicationsRoot()).thenReturn(applicationRootNode);
      when(node.getRoot()).thenReturn(applicationRootNode);
    }

    when(event.getPresentation()).thenReturn(presentation);
    return event;
  }

  @NotNull
  protected AnActionEvent createEventAndUpdateAction(ParentableNode<?> node) {
    AnActionEvent event = createEvent(node);
    AnAction action = getAction();
    action.update(event);
    return event;
  }

  private AnActionEvent setupActionOnComponent(Component component) {
    ComponentNode componentNode = mock(ComponentNode.class);
    when(componentNode.getComponent()).thenReturn(component);
    return createEventAndUpdateAction(componentNode);
  }

  private AnActionEvent setupActionOnURL(URL url) {
    URLNode urlNode = mock(URLNode.class);
    when(urlNode.getUrl()).thenReturn(url);
    return createEventAndUpdateAction(urlNode);
  }

  private ComponentInfo simpleComponentInfo() {
    return new ComponentInfo.Builder().build();
  }

  private ComponentInfo simpleComponentInfoWithSupportedFeatures(List<ComponentFeature.Mode> features) {
    return new ComponentInfo.Builder().withSupportedFeatures(features).build();
  }

  /**
   * By default, check that all actions on every node are not visible.
   * otherwise unit tests can override the verifyXYZ method to change the assertions
   *
   * @param presentation the action presentation object
   */
  private void defaultAssertions(@NotNull Presentation presentation) {
    assertFalse(presentation.isVisible());
  }

  public abstract AnAction getAction();

  /**
   * Tests Methods that will run against Action classes
   */

  @Test
  public void testActionOnLoggedInCluster() {
    ApplicationsRootNode applicationsRootNode = createApplicationRootNode(true);
    AnActionEvent event = createEventAndUpdateAction(applicationsRootNode);
    verifyLoggedInCluster(event.getPresentation());
  }

  @Test
  public void testActionOnLoggedOutCluster() {
    ApplicationsRootNode applicationsRootNode = createApplicationRootNode(false);
    AnActionEvent event = createEventAndUpdateAction(applicationsRootNode);
    verifyLoggedOutCluster(event.getPresentation());
  }

  @Test
  public void testActionOnProject() {
    NamespaceNode projectNode = mock(NamespaceNode.class);
    AnActionEvent event = createEventAndUpdateAction(projectNode);
    verifyProject(event.getPresentation());
  }

  @Test
  public void testActionOnLocalDevComponentWithNoSupportedFeatures() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV),
      ".", simpleComponentInfo()));
    verifyLocalDevComponentWithNoSupportedFeatures(event.getPresentation());
  }

  @Test
  public void testActionOnLocalDevOnPodmanComponentWithNoSupportedFeatures() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV_ON_PODMAN),
      ".", simpleComponentInfo()));
    verifyLocalDevOnPodmanComponentWithNoSupportedFeatures(event.getPresentation());
  }

  @Test
  public void testActionOnLocalDevComponentWithDevSupportedFeatures() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV),
      ".", simpleComponentInfoWithSupportedFeatures(Collections.singletonList(ComponentFeature.Mode.DEV_MODE))));
    verifyLocalDevComponentWithDevSupportedFeatures(event.getPresentation());
  }

  @Test
  public void testActionOnLocalDevOnPodmanComponentWithDevSupportedFeatures() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV_ON_PODMAN),
      ".", simpleComponentInfoWithSupportedFeatures(Collections.singletonList(ComponentFeature.Mode.DEV_MODE))));
    verifyLocalDevOnPodmanComponentWithDevSupportedFeatures(event.getPresentation());
  }

  @Test
  public void testActionOnRemoteDeployComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEPLOY),
      ".", simpleComponentInfo()));
    verifyRemoteDeployComponent(event.getPresentation());
  }

  @Test
  public void testActionOnLocalOnlyComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(), ".", simpleComponentInfo()));
    verifyLocalOnlyComponent(event.getPresentation());
  }

  @Test
  public void testActionOnRemoteOnlyDevComponent() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV), "aPath", simpleComponentInfo()));
    verifyRemoteOnlyDevComponent(event.getPresentation());
  }

  @Test
  public void testActionOnRemoteOnlyDevComponentWithoutContext() {
    AnActionEvent event = setupActionOnComponent(Component.of("comp", null, new ComponentFeatures(ComponentFeature.DEV), simpleComponentInfo()));
    verifyRemoteOnlyDevComponentWithoutContext(event.getPresentation());
  }

  @Test
  public void testActionOnService() {
    Service service = mock(Service.class);
    ServiceNode serviceNode = mock(ServiceNode.class);
    when(serviceNode.getService()).thenReturn(service);
    AnActionEvent event = createEventAndUpdateAction(serviceNode);
    verifyService(event.getPresentation());
  }

  @Test
  public void testActionOnURL() {
    AnActionEvent event = setupActionOnURL(URL.of("url1", "localhost", "8080", "8080"));
    AnAction action = getAction();
    action.update(event);
    verifyURL(event.getPresentation());
  }

  @Test
  public void testActionOnBinding() {
    Binding binding = mock(Binding.class);
    BindingNode bindingNode = mock(BindingNode.class);
    when(bindingNode.getBinding()).thenReturn(binding);
    AnActionEvent event = createEventAndUpdateAction(bindingNode);
    verifyBinding(event.getPresentation());
  }

  @Test
  public void testActionOnRegistries() {
    DevfileRegistriesNode registriesNode = mock(DevfileRegistriesNode.class);
    AnActionEvent event = createEventAndUpdateAction(registriesNode);
    verifyRegistries(event.getPresentation());
  }

  @Test
  public void testActionOnRegistry() {
    DevfileRegistryNode registryNode = mock(DevfileRegistryNode.class);
    AnActionEvent event = createEventAndUpdateAction(registryNode);
    verifyRegistry(event.getPresentation());
  }

  @Test
  public void testActionOnDevfileRegistryComponentType() {
    DevfileRegistryComponentTypeNode registryComponentTypeNode = mock(DevfileRegistryComponentTypeNode.class);
    AnActionEvent event = createEventAndUpdateAction(registryComponentTypeNode);
    verifyDevfileRegistryComponentType(event.getPresentation());
  }

  @Test
  public void testActionOnChartRelease() {
    ChartReleaseNode chartReleaseNode = mock(ChartReleaseNode.class);
    AnActionEvent event = createEventAndUpdateAction(chartReleaseNode);
    verifyChartRelease(event.getPresentation());
  }

  @Test
  public void testActionOnHelmRepositories() {
    HelmRepositoriesNode helmRepositoriesNode = mock(HelmRepositoriesNode.class);
    AnActionEvent event = createEvent(helmRepositoriesNode);
    AnAction action = getAction();
    action.update(event);
    verifyHelmRepositories(event.getPresentation().isVisible());
  }

  @Test
  public void testActionOnHelmRepository() {
    HelmRepositoryNode helmRepositoryNode = mock(HelmRepositoryNode.class);
    AnActionEvent event = createEvent(helmRepositoryNode);
    AnAction action = getAction();
    action.update(event);
    verifyHelmRepository(event.getPresentation().isVisible());
  }

  /**
   * actual verification methods that can be overridden by test classes
   */

  protected void verifyRegistries(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyRegistry(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyDevfileRegistryComponentType(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyService(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyBinding(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyURL(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyChartRelease(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyRemoteOnlyDevComponent(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyRemoteOnlyDevComponentWithoutContext(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLocalOnlyComponent(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLocalDevComponentWithNoSupportedFeatures(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLocalDevOnPodmanComponentWithNoSupportedFeatures(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLocalDevComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLocalDevOnPodmanComponentWithDevSupportedFeatures(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyRemoteDeployComponent(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLoggedInCluster(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyLoggedOutCluster(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyProject(@NotNull Presentation presentation) {
    defaultAssertions(presentation);
  }

  protected void verifyHelmRepositories(boolean visible) {
    assertFalse(visible);
  }

  protected void verifyHelmRepository(boolean visible) {
    assertFalse(visible);
  }
}
