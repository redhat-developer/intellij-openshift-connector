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
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.openapi.util.io.FileUtil;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentDescriptor;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeatures;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.OdoDelegate;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProcessHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ApplicationRootNodeOdoTest {

  public static final Component COMPONENT1 = Component.of(
    "name1",
    "managedBy1",
    new ComponentFeatures(),
    "path1",
    null);
  public static final Component COMPONENT2 = Component.of(
    "name2",
    "managedBy2",
    new ComponentFeatures(),
    "path2",
    null);
  private File tempDir;
  private final File destinationDir = new File(System.getProperty("user.home"));
  private OdoDelegate odo;
  private ApplicationsRootNode rootNode;
  private ApplicationRootNodeOdo rootNodeOdo;
  private ApplicationRootNodeOdo.FileOperations fileOperations;

  @Before
  public void before() throws IOException {
    tempDir = FileUtil.createTempDirectory("odo-test", "");
    this.odo = mock(OdoDelegate.class);
    this.rootNode = mock(ApplicationsRootNode.class);
    this.fileOperations = mockFileOperations();
    OdoProcessHelper processHelper = mock(OdoProcessHelper.class);
    this.rootNodeOdo = new ApplicationRootNodeOdo(odo, false, processHelper, rootNode, fileOperations);
  }

  @Test
  public void createComponent_should_NOT_create_files_and_copy_them_if_starter_is_blank() throws IOException {
    // given
    String componentType = "componentType";
    String registryName = "registryName";
    String component = "component";
    String source = "source";
    String devfile = "devfile";
    String starter = null;
    // when
    rootNodeOdo.createComponent(componentType, registryName, component, source, devfile, starter);
    // then
    verify(odo).createComponent(componentType, registryName, component, source, devfile, starter);
    verify(fileOperations, never()).createTempDir(any());
    verify(fileOperations, never()).copyTo(any(), any());
    verify(fileOperations, never()).refresh(any());
  }

  @Test
  public void createComponent_should_create_files_copy_and_refresh_them_if_starter_is_NOT_blank() throws IOException {
    // given
    String componentType = "componentType";
    String registryName = "registryName";
    String component = "component";
    String source = "source";
    String devfile = "devfile";
    String starter = "starter";
    // when
    rootNodeOdo.createComponent(componentType, registryName, component, source, devfile, starter);
    // then
    verify(odo).createComponent(componentType, registryName, component, tempDir.getAbsolutePath(), devfile, starter);
    verify(fileOperations).createTempDir(any());
    verify(fileOperations).copyTo(tempDir, Path.of(source));
    verify(fileOperations).refresh(destinationDir);
  }

  @Test
  public void deleteComponent_should_remove_context_if_context_is_NOT_null() throws IOException {
    // given
    String project = "project";
    String context = "context";
    String component = "component";
    ComponentKind kind = ComponentKind.DEVFILE;
    // when
    rootNodeOdo.deleteComponent(project, context, component, kind);
    // then
    verify(odo).deleteComponent(project, context, component, kind);
    verify(rootNode).removeContext(new File(context));
  }

  @Test
  public void deleteComponent_should_NOT_remove_context_if_context_is_null() throws IOException {
    // given
    String project = "project";
    String context = null;
    String component = "component";
    ComponentKind kind = ComponentKind.DEVFILE;
    // when
    rootNodeOdo.deleteComponent(project, context, component, kind);
    // then
    verify(odo).deleteComponent(project, context, component, kind);
    verify(rootNode, never()).removeContext(any());
  }

  @Test
  public void getComponents_should_get_components_from_odo_and_add_those_existing_in_root_node_if_have_different_name() throws IOException {
    // given
    String project = "project";

    doReturn(new ArrayList<>(List.of(COMPONENT1)))
      .when(odo).getComponentsOnCluster(project);

    mockGetComponents(toDescriptor(COMPONENT2), rootNode);
    // when
    List<Component> components = rootNodeOdo.getComponents(project);
    // then
    assertThat(components).hasSize(2);
    assertThat(components).contains(
      COMPONENT1,
      COMPONENT2);
  }


  @Test
  public void getComponents_should_update_component_retrieved_from_odo_if_component_with_same_name_exists_in_root() throws IOException {
    // given
    String project = "project";

    doReturn(new ArrayList<>(Arrays.asList(
      COMPONENT1,
      COMPONENT2))
    ).when(odo).getComponentsOnCluster(project);

    mockGetComponents(new ComponentDescriptor(
      COMPONENT2.getName(),
      "updatedPath",
      null,
      null), rootNode);
    // when
    List<Component> components = rootNodeOdo.getComponents(project);
    // then
    assertThat(components).hasSize(2);
    Component updatedComponent2 = Component.of(
      COMPONENT2.getName(),
      COMPONENT2.getManagedBy(),
      new ComponentFeatures(),
      "updatedPath",
      COMPONENT2.getInfo());
    assertThat(components).contains(
      COMPONENT1,
      updatedComponent2);
  }

  private ComponentDescriptor toDescriptor(Component component) {
    return new ComponentDescriptor(
      component.getName(),
      component.getPath(),
      null,
      null);
  }

  private void mockGetComponents(ComponentDescriptor descriptor, ApplicationsRootNode rootNode) {
    Map<String, ComponentDescriptor> rootNodeComponents = Map.of(
      descriptor.getPath(),
      descriptor);
    doReturn(rootNodeComponents)
      .when(rootNode).getLocalComponents();
  }

  private ApplicationRootNodeOdo.FileOperations mockFileOperations() throws IOException {
    ApplicationRootNodeOdo.FileOperations newFileOperations = mock(ApplicationRootNodeOdo.FileOperations.class);
    doReturn(tempDir)
      .when(newFileOperations).createTempDir(any());
    doReturn(destinationDir)
      .when(newFileOperations).copyTo(any(), any());
    return newFileOperations;
  }
}
