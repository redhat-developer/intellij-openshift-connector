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

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.tree.BaseTreeModel;
import com.intellij.util.messages.MessageBusConnection;
import io.fabric8.kubernetes.api.model.Context;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.RefreshableTreeModel;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.ConfigWatcher;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.odo.LocalConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.tools.intellij.openshift.Constants.ODO_CONFIG_YAML;

public class ApplicationTreeModel extends BaseTreeModel<Object> implements ConfigWatcher.Listener, RefreshableTreeModel, LazyMutableTreeNode.ChangeListener, ModuleListener {
    private ApplicationsRootNode ROOT;
    private final Project project;
    private Context context;

    public static class ComponentDescriptor {
        private final String path;
        private final String project;
        private final String application;
        private final String name;

        ComponentDescriptor(String project, String application, String path, String name) {
            this.project = project;
            this.application = application;
            this.path = path;
            this.name = name;
        }

        public String getProject() {
            return project;
        }

        public String getApplication() {
            return application;
        }

        public String getName() {
            return name;
        }

        public LocalConfig.ComponentSettings getSettings() throws IOException {
            return LocalConfig.load(new File(path, ODO_CONFIG_YAML).toURI().toURL()).getComponentSettings();
        }
    }

    private final Map<String, ComponentDescriptor> components = new HashMap();

    public ApplicationTreeModel(Project project) {
        ExecHelper.submit(new ConfigWatcher(new File(ConfigHelper.getKubeConfigPath()), this));
        ROOT = new ApplicationsRootNode(this);
        ROOT.addChangeListener(this);
        this.project = project;
        loadProjectModel(project);
        registerProjectListener(project);
        context = ConfigHelper.getCurrentContext();
    }

    public static VirtualFile getModuleRoot(Module module) {
        return LocalFileSystem.getInstance().findFileByPath(new File(module.getModuleFilePath()).getParent());
    }

    private void loadProjectModel(Project project) {
        for(Module module : project.getComponent(ModuleManager.class).getModules()) {
            addContext(getModuleRoot(module));
        }
    }

    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        addContext(getModuleRoot(module));
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        removeContext(getModuleRoot(module));
    }

    private void addContextToSettings(String path, LocalConfig.ComponentSettings componentSettings) {
        if (!components.containsKey(path)) {
            components.put(path, new ComponentDescriptor(componentSettings.getProject(), componentSettings.getApplication(), path, componentSettings.getName()));
            refresh();
        }
    }

    private void addContext(VirtualFile modulePathFile) {
        try {
            VirtualFile file = modulePathFile.findFileByRelativePath(ODO_CONFIG_YAML);
            if (file != null && file.isValid()) {
                LocalConfig config = LocalConfig.load(new File(file.getPath()).toPath().toUri().toURL());
                addContextToSettings(modulePathFile.getPath(), config.getComponentSettings());
            }
        } catch (IOException e) { }
    }

    public void addContext(String modulePath) {
        addContext(LocalFileSystem.getInstance().findFileByPath(modulePath));
    }

    private void removeContextFromSettings(String modulePath) {
        if (components.containsKey(modulePath)) {
            components.remove(modulePath);
            refresh();
        }
    }
    private void removeContext(File file) {
        if (file.exists()) {
            removeContextFromSettings(file.getPath());
        }
    }

    private void removeContext(VirtualFile modulePathFile) {
            VirtualFile file = modulePathFile.findFileByRelativePath(ODO_CONFIG_YAML);
            if (file != null && file.isValid()) {
                removeContextFromSettings(modulePathFile.getPath());
            }
    }

    private void registerProjectListener(Project project) {
        MessageBusConnection connection = project.getMessageBus().connect(project);
        connection.subscribe(ProjectTopics.MODULES, this);
    }

    public Map<String, ComponentDescriptor> getComponents() {
        return components;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public List<Object> getChildren(Object o) {
        if (o instanceof LazyMutableTreeNode) {
            LazyMutableTreeNode node = (LazyMutableTreeNode) o;
            if (!node.isLoaded()) {
                node.load();
            }
            return Collections.list(((MutableTreeNode)o).children());

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    private boolean hasContextChanged() {
        Context newContext = ConfigHelper.getCurrentContext();
        return newContext == null || context == null || !StringUtils.equals(context.getCluster(), newContext.getCluster()) || !StringUtils.equals(context.getUser(), newContext.getUser());
    }

    @Override
    public void onUpdate(ConfigWatcher source) {
        try {
            if (ConfigHelper.isKubeConfigParsable() && hasContextChanged()) {
                refresh();
            }
        } catch (Exception e) {}
    }

    @Override
    public synchronized  void refresh() {
        context = ConfigHelper.getCurrentContext();
        TreePath path = new TreePath(ROOT);
        try {
            ROOT = new ApplicationsRootNode(this);
            ROOT.addChangeListener(this);
            this.treeStructureChanged(path, new int[0], new Object[0]);
        } catch (Exception e) {
        }
    }

    @Override
    public void onChildAdded(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).addChangeListener(this);
        }
        treeNodesInserted(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildRemoved(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).removeChangeListener(this);
        }
        treeNodesRemoved(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildrensRemoved(LazyMutableTreeNode source) {
        treeStructureChanged(new TreePath(source.getPath()), new int[0], new Object[0]);
    }
}
