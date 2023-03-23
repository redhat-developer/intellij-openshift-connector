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

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.jboss.tools.intellij.openshift.utils.odo.Binding;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ApplicationsTreeStructure extends AbstractTreeStructure implements MutableModel<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsTreeStructure.class);

    private final Project project;
    private final ApplicationsRootNode root;

    private final MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();
    private DevfileRegistriesNode registries;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final String LOGIN = "Please log in to the cluster";

    private static final Supplier<Icon> CLUSTER_ICON = () -> IconLoader.findIcon("/images/cluster.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> NAMESPACE_ICON = () -> IconLoader.findIcon("/images/project.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> COMPONENT_ICON = () -> IconLoader.findIcon("/images/component.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> SERVICE_ICON = () -> IconLoader.findIcon("/images/service.png", ApplicationsTreeStructure.class);

    private static final Icon URL_ICON = IconLoader.findIcon("/images/url-node.png", ApplicationsTreeStructure.class);

    private static final Icon COMPONENT_TYPE_ICON = IconLoader.findIcon("/images/component-type-light.png", ApplicationsTreeStructure.class);

    private static final Icon STARTER_ICON = IconLoader.findIcon("/images/start-project-light.png", ApplicationsTreeStructure.class);

    private static final Icon REGISTRY_ICON = IconLoader.findIcon("/images/registry.svg", ApplicationsTreeStructure.class);

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
        if (!initialized.getAndSet(true)) {
            root.initializeOdo().thenAccept(odo -> {
                fireModified(root);
                fireModified(registries);
            });
        }
        return root;
    }

    @NotNull
    @Override
    public Object[] getChildElements(@NotNull Object element) {
        if (element == this) {
            return new Object[] {getApplicationsRoot(), registries};
        }
        Odo odo = root.getOdo();
        if (odo != null) {
            if (element instanceof ApplicationsRootNode) {
                return getNamespaces((ApplicationsRootNode) element);
            } else if (element instanceof NamespaceNode) {
                return getComponentsAndServices(((NamespaceNode) element));
            } else if (element instanceof ComponentNode) {
                List<URLNode> urls = getURLs((ComponentNode) element);
                List<BindingNode> bindings = getBindings((ComponentNode) element);
                return Stream.of(urls, bindings).filter(item -> !item.isEmpty()).flatMap(Collection::stream).toArray();
            } else if (element instanceof DevfileRegistriesNode) {
                return getRegistries(root, odo);
            } else if (element instanceof DevfileRegistryNode) {
                return getRegistryComponentTypes((DevfileRegistryNode) element);
            } else if (element instanceof DevfileRegistryComponentTypeNode) {
                return getRegistryComponentTypeStarters((DevfileRegistryComponentTypeNode) element);
            }
        }
        return new Object[0];
    }

    private Object[] getNamespaces(ApplicationsRootNode element) {
        List<Object> namespaces = new ArrayList<>();
        try {
            String ns = element.getOdo().getNamespace();
            if (ns != null) {
                namespaces.add(new NamespaceNode(element, element.getOdo().getNamespace()));
            } else {
                namespaces.add(new CreateNamespaceLinkNode(element));
            }
            element.setLogged(true);
        } catch (Exception e) {
            if (e instanceof KubernetesClientException) {
                KubernetesClientException kce = (KubernetesClientException) e;
                if (kce.getCode() == 401) {
                    namespaces.add(new MessageNode(element, element, LOGIN));
                } else if (kce.getCause() instanceof NoRouteToHostException) {
                    namespaces.add(new MessageNode(element, element, kce.getCause().getMessage()));
                } else {
                    namespaces.add(new MessageNode(element, element, "Unable to get namespaces: " + e.getMessage()));
                }
            } else {
                namespaces.add(new MessageNode(element, element, "Unable to get namespaces: " + e.getMessage()));
            }
            element.setLogged(false);
        }
        return namespaces.toArray();
    }

    private Object[] getComponentsAndServices(NamespaceNode element) {
        List<Object> results = new ArrayList<>();

        ApplicationsRootNode rootNode = element.getRoot();
        Odo odo = rootNode.getOdo();
        try {
            odo.getComponents(element.getName()).forEach(dc -> results.add(new ComponentNode(element, dc)));
        } catch (KubernetesClientException | IOException e) {
            results.add(new MessageNode(element.getRoot(), element, "Failed to load application deployment configs"));
            LOGGER.warn(e.getLocalizedMessage(),e);
        }
        try {
            odo.getServices(element.getName()).forEach(si -> results.add(new ServiceNode(element, si)));
        } catch (IOException e) {
            results.add(new MessageNode(element.getRoot(), element, "Failed to load application services"));
            LOGGER.warn(e.getLocalizedMessage(),e);
        }

        if (results.isEmpty()) {
            results.add(new CreateComponentLinkNode(element.getRoot(), element));
        }
        return results.toArray();
    }

    private List<URLNode> getURLs(ComponentNode element) {
        List<URLNode> results = new ArrayList<>();
        Odo odo = element.getRoot().getOdo();
        try {
            odo.listURLs(element.getParent().getName(),
                    element.getComponent().getPath(), element.getName()).forEach(url -> results.add(new URLNode(element, url)));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return results;
    }

    private List<BindingNode> getBindings(ComponentNode element) {
        List<BindingNode> results = new ArrayList<>();
        Odo odo = element.getRoot().getOdo();
        try {
            odo.listBindings(element.getParent().getName(),
                    element.getComponent().getPath(), element.getName()).forEach(binding -> results.add(new BindingNode(element, binding)));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return results;
    }

    private Object[] getRegistries(ApplicationsRootNode root, Odo odo) {
        List<DevfileRegistryNode> result = new ArrayList<>();

        try {
            odo.listDevfileRegistries().forEach(registry -> result.add(new DevfileRegistryNode(root, registries, registry)));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    private Object[] getRegistryComponentTypes(DevfileRegistryNode element) {
        List<DevfileRegistryComponentTypeNode> result = new ArrayList<>();
        try {
            element.getRoot().getOdo().getComponentTypes(element.getName()).forEach(type -> result.add(new DevfileRegistryComponentTypeNode(root, element, type)));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    private Object[] getRegistryComponentTypeStarters(DevfileRegistryComponentTypeNode element) {
        List<DevfileRegistryComponentTypeStarterNode> result = new ArrayList<>();
        try {
            element.getRoot().getOdo().getComponentTypeInfo(element.getName(), element.getComponentType().getDevfileRegistry().getName()).getStarters().forEach(starter -> result.add(new DevfileRegistryComponentTypeStarterNode(element.getRoot(), element, starter)));
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return result.toArray();
    }

    @Override
    public Object getParentElement(@NotNull Object element) {
        if (element instanceof ParentableNode) {
            return ((ParentableNode) element).getParent();
        }
        if (element instanceof ApplicationsRootNode) {
            return this;
        }
        return null;
    }

    @Override
    public NodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element == this) {
            return new LabelAndIconDescriptor(project, element, "Root", null, parentDescriptor);
        }
        else if (element instanceof ApplicationsRootNode) {
            ApplicationsRootNode root = (ApplicationsRootNode) element;
            return new LabelAndIconDescriptor(project, element, () -> root.getOdo() != null?root.getOdo().getMasterUrl().toString():"Loading", CLUSTER_ICON,
                    parentDescriptor);
        } else if (element instanceof NamespaceNode) {
            return new LabelAndIconDescriptor(project, element, ((NamespaceNode) element)::getName, NAMESPACE_ICON,
                    parentDescriptor);
        } else if (element instanceof ComponentNode) {
            return new LabelAndIconDescriptor(project, element,
                    () -> ((ComponentNode) element).getName() + ' ' + getComponentSuffix((ComponentNode) element),
                    COMPONENT_ICON, parentDescriptor);
        } else if (element instanceof ServiceNode) {
            return new LabelAndIconDescriptor(project, element,
                    ((ServiceNode) element)::getName, () -> ((ServiceNode) element).getService().getKind(), SERVICE_ICON, parentDescriptor);
        } else if (element instanceof URLNode) {
            URL url = ((URLNode) element).getUrl();
            return new LabelAndIconDescriptor(project, element,
                    () -> url.getName() + " (" + url.getContainerPort() + ")",
                    () -> url.asURL(),
                    () -> URL_ICON, parentDescriptor);
        } else if (element instanceof BindingNode) {
            Binding binding = ((BindingNode) element).getBinding();
            return new LabelAndIconDescriptor(project, element,
                    () -> binding.getName(),
                    () -> "Bound to " + binding.getService().getName(),
                    () -> null, parentDescriptor);
        } else if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor(project, element,((MessageNode)element).getName(), null, parentDescriptor);
        } else if (element instanceof DevfileRegistriesNode) {
            return new LabelAndIconDescriptor(project, element, "Devfile registries", REGISTRY_ICON, parentDescriptor);
        } else if (element instanceof DevfileRegistryNode) {
            return new LabelAndIconDescriptor(project, element, ((DevfileRegistryNode)element).getName(), ((DevfileRegistryNode)element).getRegistry().getURL(), REGISTRY_ICON, parentDescriptor);
        } else if (element instanceof DevfileRegistryComponentTypeNode) {
            return new LabelAndIconDescriptor(project, element, ((DevfileRegistryComponentTypeNode)element).getName(), ((DevfileRegistryComponentTypeNode)element).getComponentType().getDescription(), COMPONENT_TYPE_ICON, parentDescriptor);
        } else if (element instanceof DevfileRegistryComponentTypeStarterNode) {
            return new LabelAndIconDescriptor(project, element, ((DevfileRegistryComponentTypeStarterNode)element).getName(), ((DevfileRegistryComponentTypeStarterNode)element).getStarter().getDescription(), STARTER_ICON, parentDescriptor);
        }
        return new LabelAndIconDescriptor(project, element, element.toString(), null, parentDescriptor);
    }

    private static String getComponentSuffix(ComponentNode element) {
        Component comp = element.getComponent();
        String suffix = comp.getLiveFeatures().toString();
        if (!comp.hasContext()) {
            suffix = "no context" + (suffix.isEmpty()?"":",") + suffix;
        }
        return suffix;
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

}
