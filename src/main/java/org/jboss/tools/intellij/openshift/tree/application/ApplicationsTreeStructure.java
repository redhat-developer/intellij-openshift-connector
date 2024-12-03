/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.tree.LeafState;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ExceptionUtils;
import org.jboss.tools.intellij.openshift.utils.KubernetesClientExceptionUtils;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationsTreeStructure extends AbstractTreeStructure implements MutableModel<Object>, Disposable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsTreeStructure.class);
  private static final String LOGIN = "Please log in to the cluster";
  private static final String CLUSTER_UNREACHABLE = "Error: Cluster not reachable";
  private final Project project;
  private final ApplicationsRootNode root;
  private final MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();
  private final DevfileRegistriesNode registries;

  public ApplicationsTreeStructure(Project project, Disposable parentDisposable) {
    this.project = project;
    this.root = new ApplicationsRootNode(project, this, parentDisposable);
    this.registries = new DevfileRegistriesNode(root);
    Disposer.register(parentDisposable, this);
  }

  @Override
  public @NotNull Object getRootElement() {
    return this;
  }

  public Object getApplicationsRoot() {
    return root;
  }

  @NotNull
  @Override
  public Object @NotNull [] getChildElements(@NotNull Object element) {
    try {
      if (element == this) {
        return new Object[]{root, registries};
      } else if (element instanceof ApplicationsRootNode) {
        return new Object[]{
          getCurrentNamespace((ApplicationsRootNode) element),
          new HelmRepositoriesNode((ApplicationsRootNode) element)
        };
      } else if (element instanceof NamespaceNode) {
        return createNamespaceChildren((NamespaceNode) element);
      } else if (element instanceof HelmRepositoriesNode) {
        return createHelmRepositoriesChildren((HelmRepositoriesNode) element);
      } else if (element instanceof ComponentNode) {
        return createComponentChildren((ComponentNode) element);
      } else if (element instanceof DevfileRegistriesNode) {
        return getRegistries(root);
      } else if (element instanceof DevfileRegistryNode) {
        return getRegistryComponentTypes((DevfileRegistryNode) element);
      } else if (element instanceof DevfileRegistryComponentTypeNode) {
        return getRegistryComponentTypeStarters((DevfileRegistryComponentTypeNode) element);
      }
    } catch (Exception e) {
      return new Object[]{new MessageNode<>(root, root, ExceptionUtils.getMessage(e))};
    }
    return new Object[0];
  }

  @Override
  public @NotNull LeafState getLeafState(@NotNull Object element) {
    if (element instanceof ChartReleaseNode) {
      return LeafState.ALWAYS;
    } else if (element instanceof MessageNode<?>) {
      return LeafState.ALWAYS;
    } else {
      return LeafState.ASYNC;
    }
  }

  @NotNull
  private Object[] createComponentChildren(ComponentNode componentNode) {
    Odo odo = root.getOdo().getNow(null);
    if (odo == null) {
      return new Object[]{new MessageNode<>(root, componentNode, "Could not get components")};
    }
    List<URLNode> urls = getURLs(componentNode);
    List<BindingNode> bindings = getBindings(componentNode, odo);
    return Stream.of(urls, bindings)
      .filter(item -> !item.isEmpty())
      .flatMap(Collection::stream)
      .toArray();
  }

  @NotNull
  private Object[] createNamespaceChildren(@NotNull NamespaceNode namespaceNode) {
    OdoFacade odo = root.getOdo().getNow(null);
    if (odo == null) {
      return new MessageNode[]{new MessageNode<>(root, namespaceNode, "Could not get project children")};
    }

    if (!odo.namespaceExists(namespaceNode.getName())) {
      return new MessageNode[]{new ChangeActiveProjectLinkNode(root, namespaceNode)};
    }

    List<Object> nodes = new ArrayList<>();
    nodes.addAll(getComponents(namespaceNode, odo));
    nodes.addAll(getServices(namespaceNode, odo));
    nodes.addAll(getHelmReleases(namespaceNode));
    return nodes.toArray();
  }

  @NotNull
  private Object getCurrentNamespace(ApplicationsRootNode element) {
    Object node;
    try {
      Odo odo = root.getOdo().getNow(null);
      if (odo == null) {
        return new Object[]{new MessageNode<>(element, element, "Could not get current namespace")};
      }
      boolean isAuthorized = odo.isAuthorized();
      element.setLogged(isAuthorized);
      if (!isAuthorized) {
        node = new MessageNode<>(root, root, LOGIN);
      } else {
        String namespace = odo.getCurrentNamespace();
        if (namespace != null) {
          node = new NamespaceNode(element, namespace);
        } else {
          node = new CreateNamespaceLinkNode(element);
        }
      }
    } catch (Exception e) {
      node = createCurrentNamespaceErrorNode(element, e);
      element.setLogged(false);
    }
    return node;
  }

  private Object[] createHelmRepositoriesChildren(HelmRepositoriesNode parent) {
    Helm helm = root.getHelm();
    if (helm == null) {
      return new Object[]{new MessageNode<>(root, parent, "Could not list repositories: Helm binary missing.")};
    }
    try {
      var repositories = helm.listRepos();
      if (repositories == null) {
        return new Object[]{new MessageNode<>(root, parent, "Could not list repositories: no repositories defined.")};
      }
      return repositories.stream()
        .map(repository -> new HelmRepositoryNode(root, parent, repository))
        .toArray();
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      return new Object[]{new MessageNode<>(root, parent, "Could not list repositories: " + e.getMessage())};
    }
  }

  private MessageNode<?> createCurrentNamespaceErrorNode(ParentableNode<?> parent, Exception e) {
    if (e instanceof KubernetesClientException kce) {
      if (KubernetesClientExceptionUtils.isForbidden(kce)
        || KubernetesClientExceptionUtils.isUnauthorized(kce)) {
        return new MessageNode<>(root, parent, LOGIN);
      } else if (kce.getCause() instanceof NoRouteToHostException) {
        return new MessageNode<>(root, parent, kce.getCause().getMessage());
      } else if (kce.getCause().getMessage().contains(Constants.DEFAULT_KUBE_URL)) {
        return new MessageNode<>(root, parent, LOGIN);
      } else if (KubernetesClientExceptionUtils.isHostDown(kce)
        || KubernetesClientExceptionUtils.isConnectionReset(kce)
        || KubernetesClientExceptionUtils.isCouldNotConnect(kce)) {
        return new MessageNode<>(root, parent, CLUSTER_UNREACHABLE);
      }
    }
    return new MessageNode<>(root, parent, "Could not get current namespace: " + ExceptionUtils.getMessage(e));
  }

  private List<BaseNode<?>> getComponents(NamespaceNode namespaceNode, OdoFacade odo) {
    List<BaseNode<?>> components = new ArrayList<>(load(
      () -> odo.getComponents(namespaceNode.getName()).stream()
        .filter(component -> !component.isManagedByHelm()) // dont display helm components
        .map(component -> new ComponentNode(namespaceNode, component))
        .collect(Collectors.toList()),
      namespaceNode,
      "Could not get components"));
    if (components.isEmpty()) {
      components.add(new CreateComponentLinkNode(namespaceNode.getRoot(), namespaceNode));
    }
    return components;
  }

  private List<BaseNode<?>> getServices(NamespaceNode namespaceNode, Odo odo) {
    return load(() -> odo.getServices(namespaceNode.getName()).stream()
        .map(si -> new ServiceNode(namespaceNode, si))
        .collect(Collectors.toList()),
      namespaceNode,
      "Could not get application services");
  }

  private List<BaseNode<?>> getHelmReleases(NamespaceNode namespaceNode) {
    Helm helm = namespaceNode.getRoot().getHelm();
    if (helm == null) {
      return List.of(new MessageNode<>(root, namespaceNode, "Could not get chart releases"));
    }
    return load(() -> helm.list().stream()
        .map(release -> new ChartReleaseNode(namespaceNode, release))
        .collect(Collectors.toList()),
      namespaceNode,
      "Could not get chart releases");
  }

  private List<BaseNode<?>> load(Callable<List<BaseNode<?>>> callable, NamespaceNode namespace, String errorMessage) {
    try {
      return callable.call();
    } catch (Exception e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
      return Collections.singletonList(new MessageNode<>(namespace.getRoot(), namespace, errorMessage));
    }
  }

  private List<URLNode> getURLs(ComponentNode element) {
    List<URLNode> results = new ArrayList<>();
    Odo odo = element.getRoot().getOdo().getNow(null);
    if (odo == null) {
      return Collections.emptyList();
    }
    try {
      odo.listURLs(element.getComponent().getPath())
        .forEach(url ->
          results.add(new URLNode(element, url))
        );
    } catch (IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
    return results;
  }

  private List<BindingNode> getBindings(ComponentNode element, @NotNull Odo odo) {
    List<BindingNode> results = new ArrayList<>();
    try {
      odo.listBindings(element.getComponent().getPath())
        .forEach(binding ->
          results.add(new BindingNode(element, binding))
        );
    } catch (IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
    return results;
  }

  private Object[] getRegistries(ApplicationsRootNode root) {
    Odo odo = root.getOdo().getNow(null);
    if (odo == null) {
      return new Object[]{new MessageNode<>(root, root, "Could not get registries")};
    }
    List<DevfileRegistryNode> result = new ArrayList<>();
    try {
      odo.listDevfileRegistries().forEach(registry ->
        result.add(new DevfileRegistryNode(root, registries, registry))
      );
    } catch (IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
    return result.toArray();
  }

  private Object[] getRegistryComponentTypes(DevfileRegistryNode registryNode) {
    Odo odo = root.getOdo().getNow(null);
    if (odo == null) {
      return new Object[]{new MessageNode<>(root, registryNode, "Could not get registry component types")};
    }
    List<DevfileRegistryComponentTypeNode> result = new ArrayList<>();
    try {
      odo.getComponentTypesFromRegistry(registryNode.getName()).forEach(type ->
        result.add(new DevfileRegistryComponentTypeNode(root, registryNode, type)));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage(), e);
    }
    return result.toArray();
  }

  private Object[] getRegistryComponentTypeStarters(DevfileRegistryComponentTypeNode componentTypeNode) {
    Odo odo = root.getOdo().getNow(null);
    if (odo == null) {
      return new Object[]{new MessageNode<>(root, componentTypeNode, "Could not get registry component starters")};
    }
    List<DevfileRegistryComponentTypeStarterNode> result = new ArrayList<>();
    try {
      odo.getComponentTypeInfo(componentTypeNode.getName(),
        componentTypeNode.getComponentType().getDevfileRegistry().getName()).getStarters().forEach(starter ->
        result.add(new DevfileRegistryComponentTypeStarterNode(componentTypeNode.getRoot(), componentTypeNode, starter)));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage(), e);
    }
    return result.toArray();
  }

  @Override
  public Object getParentElement(@NotNull Object element) {
    if (element instanceof ApplicationsRootNode) {
      return this;
    }
    if (element instanceof ParentableNode) {
      return ((ParentableNode<?>) element).getParent();
    }
    return null;
  }

  @Override
  public @NotNull NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
    return DescriptorFactory.create(element, parentDescriptor, this, project);
  }

  @Override
  public void commit() {
  }

  @Override
  public boolean hasSomethingToCommit() {
    return false;
  }

  @Override
  public void fireAdded(Object element) {
    mutableModelSupport.fireAdded(element);
  }

  @Override
  public void fireModified(Object element) {
    mutableModelSupport.fireModified(element);
  }

  @Override
  public void fireRemoved(Object element) {
    mutableModelSupport.fireRemoved(element);
  }

  @Override
  public void addListener(Listener<Object> listener) {
    mutableModelSupport.addListener(listener);
  }

  @Override
  public void removeListener(Listener<Object> listener) {
    mutableModelSupport.removeListener(listener);
  }

  @Override
  public void dispose() {
    root.dispose();
  }

  public static class ProcessableDescriptor<T> extends LabelAndIconDescriptor<T> {

    public ProcessableDescriptor(Project project, T element, Supplier<String> label, Supplier<String> location, Supplier<Icon> nodeIcon, @Nullable NodeDescriptor parentDescriptor) {
      super(project, element, label, location, nodeIcon, parentDescriptor);
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
      if (isProcessing()) {
        String processingLabel = getProcessingMessage();
        if (processingLabel != null) {
          presentation.setLocationString(processingLabel);
        }
      }
      super.update(presentation);
    }

    private String getProcessingMessage() {
      ProcessingNode node = getProcessingNode();
      if (node != null) {
        return node.getMessage();
      } else {
        return null;
      }
    }

    private boolean isProcessing() {
      ProcessingNode node = getProcessingNode();
      return node != null
        && node.isProcessing();
    }

    private ProcessingNode getProcessingNode() {
      Object element = getElement();
      if (element instanceof ProcessingNode) {
        return (ProcessingNode) element;
      } else {
        return null;
      }
    }
  }
}
