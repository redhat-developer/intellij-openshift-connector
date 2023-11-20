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
import com.intellij.ui.tree.LeafState;
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
        Odo odo = root.getOdo().getNow(null);
        if (odo != null) {
            if (element == this) {
                return new Object[]{getApplicationsRoot(), registries};
            } else if (element instanceof ApplicationsRootNode) {
                return getCurrentNamespace((ApplicationsRootNode) element, odo);
            } else if (element instanceof NamespaceNode) {
                return createNamespaceChildren((NamespaceNode) element, odo);
            } else if (element instanceof ComponentNode) {
                return createComponentChildren((ComponentNode) element, odo);
            } else if (element instanceof DevfileRegistriesNode) {
                return getRegistries(root, odo);
            } else if (element instanceof DevfileRegistryNode) {
                return getRegistryComponentTypes((DevfileRegistryNode) element, odo);
            } else if (element instanceof DevfileRegistryComponentTypeNode) {
                return getRegistryComponentTypeStarters((DevfileRegistryComponentTypeNode) element, odo);
            }
        }
        return new Object[0];
    }

    @Override
    public @NotNull LeafState getLeafState(@NotNull Object element) {
        if (element instanceof ComponentNode) {
            return LeafState.ALWAYS;
        } else if (element instanceof ChartReleaseNode) {
            return LeafState.ALWAYS;
        } else if (element instanceof MessageNode<?>) {
            return LeafState.ALWAYS;
        } else {
            return LeafState.ASYNC;
        }
    }

    @NotNull
    private Object[] createComponentChildren(ComponentNode element, @NotNull Odo odo) {
        List<URLNode> urls = getURLs(element);
        List<BindingNode> bindings = getBindings(element, odo);
        return Stream.of(urls, bindings)
          .filter(item -> !item.isEmpty())
          .flatMap(Collection::stream)
          .toArray();
    }

    @NotNull
    private Object[] createNamespaceChildren(@NotNull NamespaceNode namespaceNode, @NotNull Odo odo) {
        List<Object> nodes = new ArrayList<>();

        nodes.addAll(getComponents(namespaceNode, odo));
        nodes.addAll(getServices(namespaceNode, odo));

        Helm helm = namespaceNode.getRoot().getHelm(true).getNow(null);
        nodes.addAll(getHelmReleases(namespaceNode, helm));

        return nodes.toArray();
    }

    private Object[] getCurrentNamespace(ApplicationsRootNode element, @NotNull Odo odo) {
        List<Object> namespaces = new ArrayList<>();
        try {
            String ns = odo.getCurrentNamespace();
            if (ns != null) {
                namespaces.add(new NamespaceNode(element, ns));
            } else {
                namespaces.add(new CreateNamespaceLinkNode(element));
            }
            element.setLogged(true);
        } catch (Exception e) {
            namespaces.add(createErrorNode(element, e));
            element.setLogged(false);
        }
        return namespaces.toArray();
    }

    private static MessageNode<?> createErrorNode(ApplicationsRootNode element, Exception e) {
        if (e instanceof KubernetesClientException) {
            KubernetesClientException kce = (KubernetesClientException) e;
            if (kce.getCode() == 401) {
                return new MessageNode<>(element, element, LOGIN);
            } else if (kce.getCause() instanceof NoRouteToHostException) {
                return new MessageNode<>(element, element, kce.getCause().getMessage());
            } else if (kce.getCause().getMessage().contains(Constants.DEFAULT_KUBE_URL)) {
                return new MessageNode<>(element, element, LOGIN);
            }
        }
        return new MessageNode<>(element, element, "Unable to get namespaces: " + e.getMessage());
    }

    private List<BaseNode<?>> getComponents(NamespaceNode element, Odo odo) {
        if (odo == null) {
            return Collections.emptyList();
        }
        List<BaseNode<?>> components = new ArrayList<>();
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

    private List<BaseNode<?>> getServices(NamespaceNode element, Odo odo) {
        if (odo == null) {
            return Collections.emptyList();
        }
        return load(() -> odo.getServices(element.getName()).stream()
            .map(si -> new ServiceNode(element, si))
            .collect(Collectors.toList()),
          element,
          "Failed to load application services");
    }

    private List<BaseNode<?>> getHelmReleases(NamespaceNode element, Helm helm) {
        if (helm == null) {
            return Collections.emptyList();
        }
        return load(() -> helm.list().stream()
            .map(release -> new ChartReleaseNode(element, release))
            .collect(Collectors.toList()),
          element,
          "Failed to load chart releases");
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
            odo.listURLs(element.getParent().getName(),
                element.getComponent().getPath(), element.getName()).forEach(url ->
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
            odo.listBindings(element.getParent().getName(),
                element.getComponent().getPath(), element.getName()).forEach(binding ->
                    results.add(new BindingNode(element, binding))
                );
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return results;
    }

    private Object[] getRegistries(ApplicationsRootNode root, @NotNull Odo odo) {
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

    private Object[] getRegistryComponentTypes(DevfileRegistryNode element, @NotNull Odo odo) {
        List<DevfileRegistryComponentTypeNode> result = new ArrayList<>();
        try {
            odo.getComponentTypes(element.getName()).forEach(type ->
              result.add(new DevfileRegistryComponentTypeNode(root, element, type)));
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    private Object[] getRegistryComponentTypeStarters(DevfileRegistryComponentTypeNode element, @NotNull Odo odo) {
        List<DevfileRegistryComponentTypeStarterNode> result = new ArrayList<>();
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
