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
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
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

public class ApplicationsTreeStructure extends AbstractTreeStructure implements MutableModel<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsTreeStructure.class);

    private final Project project;
    private final ApplicationsRootNode root;

    private final MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();
    private final DevfileRegistriesNode registries;

    private static final String LOGIN = "Please log in to the cluster";

    public ApplicationsTreeStructure(Project project) {
        this.project = project;
        this.root = new ApplicationsRootNode(project, this);
        this.registries = new DevfileRegistriesNode(root);
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
        if (element == this) {
            return new Object[]{getApplicationsRoot(), registries};
        }
        if (element instanceof ApplicationsRootNode) {
            return getNamespaces((ApplicationsRootNode) element);
        } else if (element instanceof NamespaceNode) {
            return createNamespaceChildren((NamespaceNode) element);
        } else if (element instanceof ComponentNode) {
            return createComponentChildren((ComponentNode) element);
        } else if (element instanceof DevfileRegistriesNode) {
            return getRegistries(root);
        } else if (element instanceof DevfileRegistryNode) {
            return getRegistryComponentTypes((DevfileRegistryNode) element);
        } else if (element instanceof DevfileRegistryComponentTypeNode) {
            return getRegistryComponentTypeStarters((DevfileRegistryComponentTypeNode) element);
        }
        return new Object[0];
    }

    @NotNull
    private Object[] createComponentChildren(ComponentNode element) {
        List<URLNode> urls = getURLs(element);
        List<BindingNode> bindings = getBindings(element);
        return Stream.of(urls, bindings)
          .filter(item -> !item.isEmpty())
          .flatMap(Collection::stream)
          .toArray();
    }

    @NotNull
    private Object[] createNamespaceChildren(@NotNull NamespaceNode namespaceNode) {
        List<Object> nodes = new ArrayList<>();

        Odo odo = namespaceNode.getRoot().getOdo().getNow(null);
        nodes.addAll(getComponents(namespaceNode, odo));
        nodes.addAll(getServices(namespaceNode, odo));

        Helm helm = namespaceNode.getRoot().getHelm(true).getNow(null);
        nodes.addAll(getHelmReleases(namespaceNode, helm));

        return nodes.toArray();
    }

    private Object[] getNamespaces(ApplicationsRootNode element) {
        List<Object> namespaces = new ArrayList<>();
        try {
            Odo odo = element.getOdo().getNow(null);
            if (odo == null) {
                return new Object[]{};
            }
            String ns = odo.getNamespace();
            if (ns != null) {
                namespaces.add(new NamespaceNode(element, odo.getNamespace()));
            } else {
                namespaces.add(new CreateNamespaceLinkNode(element));
            }
            element.setLogged(true);
        } catch (Exception e) {
            if (e instanceof KubernetesClientException) {
                KubernetesClientException kce = (KubernetesClientException) e;
                if (kce.getCode() == 401) {
                    namespaces.add(new MessageNode<>(element, element, LOGIN));
                } else if (kce.getCause() instanceof NoRouteToHostException) {
                    namespaces.add(new MessageNode<>(element, element, kce.getCause().getMessage()));
                } else if (kce.getCause().getMessage().contains(Constants.DEFAULT_KUBE_URL)) {
                    namespaces.add(new MessageNode<>(element, element, LOGIN));
                } else {
                    namespaces.add(new MessageNode<>(element, element, "Unable to get namespaces: " + e.getMessage()));
                }
            } else {
                namespaces.add(new MessageNode<>(element, element, "Unable to get namespaces: " + e.getMessage()));
            }
            element.setLogged(false);
        }
        return namespaces.toArray();
    }

    private List<Object> getComponents(NamespaceNode element, Odo odo) {
        if (odo == null) {
            return Collections.emptyList();
        }
        List<Object> components = new ArrayList<>();
        components.addAll(load(
          () -> odo.getComponents(element.getName()).stream()
            .filter(component -> !component.isManagedByHelm()) // dont display helm components
            .map(component -> new ComponentNode(element, component))
            .collect(Collectors.toList()),
          element,
          "Failed to load components"));
        if (components.isEmpty()) {
            components.add(new CreateComponentLinkNode(element.getRoot(), element));
        }
        return components;
    }

    private List<Object> getServices(NamespaceNode element, Odo odo) {
        if (odo == null) {
            return Collections.emptyList();
        }
        List<Object> services = new ArrayList<>();
        services.addAll(load(
          () -> odo.getServices(element.getName()).stream()
            .map(si -> new ServiceNode(element, si))
            .collect(Collectors.toList()),
          element,
          "Failed to load application services"));
        return services;
    }

    private List<Object> getHelmReleases(NamespaceNode element, Helm helm) {
        List<Object> helmReleases = new ArrayList<>();
        if (helm == null) {
            return Collections.emptyList();
        }
        helmReleases.addAll(load(
          () -> helm.list().stream()
            .map(release -> new ChartReleaseNode(element, release))
            .collect(Collectors.toList()),
          element,
          "Failed to load chart releases"));
        return helmReleases;
    }

    private Collection<ParentableNode<?>> load(Callable<Collection<ParentableNode<?>>> callable, NamespaceNode namespace, String errorMessage) {
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
            odo.listURLs(element.getParent().getName(),
                element.getComponent().getPath(), element.getName()).forEach(url ->
                    results.add(new URLNode(element, url))
                );
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return results;
    }

    private List<BindingNode> getBindings(ComponentNode element) {
        List<BindingNode> results = new ArrayList<>();
        Odo odo = element.getRoot().getOdo().getNow(null);
        if (odo == null) {
            return Collections.emptyList();
        }
        try {
            odo.listBindings(element.getParent().getName(),
                element.getComponent().getPath(), element.getName()).forEach(binding ->
                    results.add(new BindingNode(element, binding))
                );
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return results;
    }

    private Object[] getRegistries(ApplicationsRootNode root) {
        List<DevfileRegistryNode> result = new ArrayList<>();
        Odo odo = root.getOdo().getNow(null);
        try {
            odo.listDevfileRegistries().forEach(registry ->
                result.add(new DevfileRegistryNode(root, registries, registry))
            );
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    private Object[] getRegistryComponentTypes(DevfileRegistryNode element) {
        List<DevfileRegistryComponentTypeNode> result = new ArrayList<>();
        Odo odo = element.getRoot().getOdo().getNow(null);
        if (odo == null) {
            return new Object[]{};
        }
        try {
            odo.getComponentTypes(element.getName()).forEach(type ->
              result.add(new DevfileRegistryComponentTypeNode(root, element, type)));
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    private Object[] getRegistryComponentTypeStarters(DevfileRegistryComponentTypeNode element) {
        List<DevfileRegistryComponentTypeStarterNode> result = new ArrayList<>();
        Odo odo = element.getRoot().getOdo().getNow(null);
        if (odo == null) {
            return new Object[]{};
        }
        try {
            odo.getComponentTypeInfo(element.getName(),
              element.getComponentType().getDevfileRegistry().getName()).getStarters().forEach(starter ->
              result.add(new DevfileRegistryComponentTypeStarterNode(element.getRoot(), element, starter)));
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    @Override
    public Object getParentElement(@NotNull Object element) {
        if (element instanceof ParentableNode) {
            return ((ParentableNode<?>) element).getParent();
        }
        if (element instanceof ApplicationsRootNode) {
            return this;
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

    public static class ProcessableDescriptor<T> extends LabelAndIconDescriptor<T> {

        public ProcessableDescriptor(Project project, T element, Supplier<String> label, Supplier<String> location, Supplier<Icon> nodeIcon, @Nullable NodeDescriptor parentDescriptor) {
            super(project, element, label, location, nodeIcon, parentDescriptor);
        }

        @Override
        protected void update(@NotNull PresentationData presentation) {
            super.update(presentation);
            if (isProcessing()) {
                String processingLabel = getProcessingMessage();
                if (processingLabel != null) {
                    presentation.setLocationString(processingLabel);
                }
            }
        }

        private String getProcessingMessage() {
            ParentableNode<T> modelNode = getParentableNode();
            if (modelNode != null) {
                return modelNode.getProcessingMessage();
            } else {
                return null;
            }
        }

        private boolean isProcessing() {
            ParentableNode<?> node = getParentableNode();
            return node != null
                && node.isProcessing();
        }

        private ParentableNode<T> getParentableNode() {
            Object element = getElement();
            if (element instanceof ParentableNode) {
                return (ParentableNode<T>) element;
            } else {
                return null;
            }
        }
    }

}
