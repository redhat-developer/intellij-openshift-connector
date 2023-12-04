/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import org.jboss.tools.intellij.openshift.ui.helm.ChartIcons;
import org.jboss.tools.intellij.openshift.utils.odo.Binding;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.function.Supplier;

public class DescriptorFactory {

  private static final Supplier<Icon> CLUSTER_ICON = () -> IconLoader.findIcon("/images/cluster.png", ApplicationsTreeStructure.class);
  private static final Supplier<Icon> NAMESPACE_ICON = () -> IconLoader.findIcon("/images/project.png", ApplicationsTreeStructure.class);
  private static final Supplier<Icon> COMPONENT_ICON = () -> IconLoader.findIcon("/images/component.png", ApplicationsTreeStructure.class);
  private static final Supplier<Icon> SERVICE_ICON = () -> IconLoader.findIcon("/images/service.png", ApplicationsTreeStructure.class);
  private static final Icon URL_ICON = IconLoader.findIcon("/images/url-node.png", ApplicationsTreeStructure.class);
  private static final Icon COMPONENT_TYPE_ICON = IconLoader.findIcon("/images/component-type-light.png", ApplicationsTreeStructure.class);
  private static final Icon STARTER_ICON = IconLoader.findIcon("/images/start-project-light.png", ApplicationsTreeStructure.class);
  private static final Icon REGISTRY_ICON = IconLoader.findIcon("/images/registry.svg", ApplicationsTreeStructure.class);

  public static @NotNull NodeDescriptor<?> create(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor, @NotNull ApplicationsTreeStructure structure, @NotNull Project project) {
    if (element == structure) {
      return new LabelAndIconDescriptor<>(
        project,
        element,
        "Root",
        null,
        parentDescriptor);
    } else if (element instanceof ApplicationsRootNode) {
      ApplicationsRootNode root = (ApplicationsRootNode) element;
      return new ApplicationsTreeStructure.ProcessableDescriptor<>(
        project,
        root,
        () -> {
          try {
            Odo odo = root.getOdo().getNow(null);
            if (odo == null) {
              return "Loading...";
            }
            java.net.URL masterUrl = odo.getMasterUrl();
            if ("kubernetes.default.svc".equals(masterUrl.getHost())) {
              return "no (current) context/cluster set";
            } else {
              return masterUrl.toString();
            }
          } catch (Exception e) {
            return "Error: " + e.getCause().getMessage();
          }
        },
        null,
        CLUSTER_ICON,
        parentDescriptor);
    } else if (element instanceof NamespaceNode) {
      NamespaceNode namespaceNode = (NamespaceNode) element;
      return new ApplicationsTreeStructure.ProcessableDescriptor<>(
        project,
        namespaceNode,
        namespaceNode::getName,
        null,
        NAMESPACE_ICON,
        parentDescriptor);
    } else if (element instanceof ComponentNode) {
      ComponentNode componentNode = (ComponentNode) element;
      return new ApplicationsTreeStructure.ProcessableDescriptor<>(
        project,
        componentNode,
        componentNode::getName,
        () -> getComponentSuffix(componentNode),
        COMPONENT_ICON,
        parentDescriptor);
    } else if (element instanceof ServiceNode) {
      ServiceNode serviceNode = (ServiceNode) element;
      return new ApplicationsTreeStructure.ProcessableDescriptor<>(
        project,
        serviceNode,
        serviceNode::getName,
        () -> serviceNode.getService().getKind(),
        SERVICE_ICON,
        parentDescriptor);
    } else if (element instanceof URLNode) {
      URL url = ((URLNode) element).getUrl();
      return new LabelAndIconDescriptor<>(
        project,
        (URLNode) element,
        () -> url.getName() + " (" + url.getContainerPort() + ")",
        url::asURL,
        () -> URL_ICON, parentDescriptor);
    } else if (element instanceof BindingNode) {
      Binding binding = ((BindingNode) element).getBinding();
      return new LabelAndIconDescriptor<>(project, (BindingNode) element,
        binding::getName,
        () -> "Bound to " + binding.getService().getName(),
        () -> null, parentDescriptor);
    } else if (element instanceof MessageNode) {
      return new LabelAndIconDescriptor<>(
        project,
        (MessageNode<?>) element,
        ((MessageNode<?>) element).getName(),
        null,
        parentDescriptor);
    } else if (element instanceof DevfileRegistriesNode) {
      return new LabelAndIconDescriptor<>(
        project,
        (DevfileRegistriesNode) element,
        "Devfile registries",
        REGISTRY_ICON,
        parentDescriptor);
    } else if (element instanceof DevfileRegistryNode) {
      DevfileRegistryNode regNode = (DevfileRegistryNode) element;
      return new ApplicationsTreeStructure.ProcessableDescriptor<>(
        project,
        regNode,
        regNode::getName,
        () -> regNode.getRegistry().getURL(),
        () -> REGISTRY_ICON,
        parentDescriptor);
    } else if (element instanceof DevfileRegistryComponentTypeNode) {
      DevfileRegistryComponentTypeNode typeNode = (DevfileRegistryComponentTypeNode) element;
      return new LabelAndIconDescriptor<>(
        project,
        typeNode,
        typeNode.getName(),
        typeNode.getComponentType().getDescription(),
        COMPONENT_TYPE_ICON,
        parentDescriptor);
    } else if (element instanceof DevfileRegistryComponentTypeStarterNode) {
      DevfileRegistryComponentTypeStarterNode starterNode = (DevfileRegistryComponentTypeStarterNode) element;
      return new LabelAndIconDescriptor<>(
        project,
        starterNode,
        starterNode.getName(),
        starterNode.getStarter().getDescription(),
        STARTER_ICON,
        parentDescriptor);
    } else if (element instanceof ChartReleaseNode) {
      ChartReleaseNode releaseNode = (ChartReleaseNode) element;
      return new ApplicationsTreeStructure.ProcessableDescriptor<>(
        project,
        releaseNode,
        releaseNode::getName,
        () -> "Helm Release",
        () -> ChartIcons.getIcon15x15(releaseNode.getRelease()),
        parentDescriptor);
    }

    return new LabelAndIconDescriptor<>(
      project,
      element,
      element.toString(),
      null,
      parentDescriptor);
  }

  private static String getComponentSuffix(ComponentNode element) {
    Component comp = element.getComponent();
    if (comp.hasContext() && !comp.getLiveFeatures().isOnCluster()) {
      return "locally created";
    }
    String suffix = comp.getLiveFeatures().toString();
    if (!comp.hasContext()) {
      suffix = "no local context" + (suffix.isEmpty() ? "" : ", ") + suffix;
    }
    return suffix;
  }

}
