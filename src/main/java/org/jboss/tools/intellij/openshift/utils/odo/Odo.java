/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public interface Odo {
  List<String> getNamespaces() throws IOException;

  String getCurrentNamespace();

  boolean namespaceExists(String name);

  String getNamespaceKind();

  void describeComponent(String context) throws IOException;

  List<ComponentMetadata> analyze(String path) throws IOException;

  void createComponent(String componentType, String registryName, String component, String source, String devfile, String starter) throws IOException;

  void createService(String project, ServiceTemplate serviceTemplate, OperatorCRD serviceCRD,
                     String service, ObjectNode spec, boolean wait) throws IOException;

  void deleteService(String project, Service service) throws IOException;

  List<DevfileComponentType> getAllComponentTypes() throws IOException;

  List<DevfileComponentType> getComponentTypesFromRegistry(String name) throws IOException;

  List<ServiceTemplate> getServiceTemplates() throws IOException;

  List<URL> listURLs(String context) throws IOException;

  ComponentInfo getComponentInfo(String project, String component, String path, ComponentKind kind) throws IOException;

  void deleteComponent(String project, String context, String component, ComponentKind kind) throws IOException;

  boolean isLogRunning(String component, boolean deploy);

  void createProject(String project) throws IOException;

  void deleteProject(String project) throws IOException;

  void setProject(String project) throws IOException;

  void login(String url, String userName, char[] password, char[] token) throws IOException;

  boolean isAuthorized();

  List<Service> getServices(String project) throws IOException;

  void about() throws IOException;

  Binding link(String context, String target) throws IOException;

  List<Binding> listBindings(String context) throws IOException;

  void deleteBinding(String context, String binding) throws IOException;

  String consoleURL() throws IOException;

  void debug(String context, Integer port) throws IOException;

  java.net.URL getMasterUrl();

  List<ComponentDescriptor> discover(String path) throws IOException;

  ComponentTypeInfo getComponentTypeInfo(String componentType, String registryName) throws IOException;

  List<DevfileRegistry> listDevfileRegistries() throws IOException;

  void createDevfileRegistry(String name, String url, String token) throws IOException;

  void deleteDevfileRegistry(String name) throws IOException;

  boolean isOpenShift();

  void migrateComponent(String name);
}
