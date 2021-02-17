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
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.URL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class ApplicationsTreeStructure extends AbstractTreeStructure implements MutableModel<Object> {
    private final Project project;
    private ApplicationsRootNode root;

    private MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final String LOGIN = "Please log in to the cluster";
    private static final String FAILED_TO_LOAD_APPLICATIONS = "Failed to load applications";

    private static final Supplier<Icon> CLUSTER_ICON = () -> IconLoader.findIcon("/images/cluster.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> NAMESPACE_ICON = () -> IconLoader.findIcon("/images/project.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> APPLICATION_ICON = () -> IconLoader.findIcon("/images/application.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> COMPONENT_ICON = () -> IconLoader.findIcon("/images/component.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> SERVICE_ICON = () -> IconLoader.findIcon("/images/service.png", ApplicationsTreeStructure.class);

    private static final Supplier<Icon> STORAGE_ICON = () -> IconLoader.findIcon("/images/storage.png", ApplicationsTreeStructure.class);

    private static final Icon URL_ICON = IconLoader.findIcon("/images/url-node.png", ApplicationsTreeStructure.class);
    private static final Icon URL_SECURE_ICON = IconLoader.findIcon("/images/url-node-secure.png", ApplicationsTreeStructure.class);

    public ApplicationsTreeStructure(Project project) {
        this.project = project;
        this.root = new ApplicationsRootNode(project, this);
    }

    @Override
    public @NotNull Object getRootElement() {
        if (!initialized.getAndSet(true)) {
            root.initializeOdo().thenAccept(tkn -> fireModified(root));
        }
        return root;    }

    @Override
    public Object[] getChildElements(@NotNull Object element) {
        Odo odo = root.getOdo();
        if (odo != null) {
            if (element instanceof ApplicationsRootNode) {
                return getNamespaces((ApplicationsRootNode) element);
            } else if (element instanceof NamespaceNode) {
                return getApplications(((NamespaceNode) element));
            } else if (element instanceof ApplicationNode) {
                return getComponentsAndServices((ApplicationNode) element);
            } else if (element instanceof ComponentNode) {
                return getStoragesAndURLs((ComponentNode) element);
            }
        }
        return new Object[0];
    }

    private Object[] getNamespaces(ApplicationsRootNode element) {
        List<Object> namespaces = new ArrayList<>();
        try {
            element.getOdo().getProjects().forEach(p -> namespaces.add(new NamespaceNode(element, p.getMetadata().getName())));
            element.setLogged(true);
        } catch (Exception e) {
            namespaces.add(new MessageNode(element, element, LOGIN));
        }
        return namespaces.toArray();
    }

    private Object[] getApplications(NamespaceNode element) {
        List<Object> applications = new ArrayList<>();
        try {
            element.getParent().getOdo().getApplications(element.getName()).forEach(app -> applications.add(new ApplicationNode(element, app.getName())));
        } catch (IOException e) {
            applications.add(new MessageNode(element.getRoot(), element, FAILED_TO_LOAD_APPLICATIONS));
        }
        return applications.toArray();
    }

    private Object[] getComponentsAndServices(ApplicationNode element) {
        List<Object> results = new ArrayList<>();
        try {
            ApplicationsRootNode rootNode = element.getRoot();
            Odo odo = rootNode.getOdo();
            try {
                odo.getComponents(element.getParent().getName(), element.getName()).forEach(dc -> results.add(new ComponentNode(element, dc)));
            } catch (KubernetesClientException e) {
                results.add(new MessageNode(element.getRoot(), element, "Failed to load application deployment configs"));
            }
            odo.getServices(element.getParent().getName(), element.getName()).forEach(si -> results.add(new ServiceNode(element, si)));
        } catch (IOException e) {
            results.add(new MessageNode(element.getRoot(), element, "Failed to load application"));
        }
        return results.toArray();
    }

    private Object[] getStoragesAndURLs(ComponentNode element) {
        List<Object> results = new ArrayList<>();
        Odo odo = element.getRoot().getOdo();
        try {
            odo.getStorages(element.getParent().getParent().getName(), element.getParent().getName(),
                    element.getComponent().getPath(), element.getName()).forEach(storage -> results.add(new PersistentVolumeClaimNode(element, storage)));
        } catch (KubernetesClientException | IOException e) {
        }
        try {
            odo.listURLs(element.getParent().getParent().getName(), element.getParent().getName(),
                    element.getComponent().getPath(), element.getName()).forEach(url -> results.add(new URLNode(element, url)));
        } catch (IOException e) {
        }
        return results.toArray();
    }

    @Override
    public Object getParentElement(@NotNull Object element) {
        if (element instanceof ParentableNode) {
            return ((ParentableNode)element).getParent();
        }
        return null;    }

    @Override
    public NodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof ApplicationsRootNode) {
            Odo odo = ((ApplicationsRootNode)element).getOdo();
            return new LabelAndIconDescriptor(project, element, () -> odo != null?odo.getMasterUrl().toString():"Loading", CLUSTER_ICON,
                    parentDescriptor);
        } else if (element instanceof NamespaceNode) {
            return new LabelAndIconDescriptor(project, element, ((NamespaceNode)element)::getName, NAMESPACE_ICON,
                    parentDescriptor);
        } else if (element instanceof ApplicationNode) {
            return new LabelAndIconDescriptor(project, element, ((ApplicationNode)element)::getName, APPLICATION_ICON,
                    parentDescriptor);
        } else if (element instanceof ComponentNode) {
            return new LabelAndIconDescriptor(project, element,
                    () -> ((ComponentNode)element).getName() + ' ' + ((ComponentNode)element).getComponent().getState(),
                    COMPONENT_ICON, parentDescriptor);
        } else if (element instanceof ServiceNode) {
            return new LabelAndIconDescriptor(project, element,
                    ((ServiceNode)element)::getName, SERVICE_ICON, parentDescriptor);
        } else if (element instanceof PersistentVolumeClaimNode) {
            return new LabelAndIconDescriptor(project, element,
                    ((PersistentVolumeClaimNode)element)::getName, STORAGE_ICON, parentDescriptor);
        } else if (element instanceof URLNode) {
            URL url = ((URLNode)element).getUrl();
            return new LabelAndIconDescriptor(project, element,
                    () -> url.getName() + " (" + url.getPort() + ") (" + url.getState() + ')',
                    () -> url.isSecure()?URL_SECURE_ICON:URL_ICON, parentDescriptor);
        } else if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor(project, element,((MessageNode)element).getName(), null, parentDescriptor);
        }
        return null;
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
