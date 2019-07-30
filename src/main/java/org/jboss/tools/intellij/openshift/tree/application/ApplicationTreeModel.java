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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.tree.BaseTreeModel;
import com.intellij.util.messages.MessageBusConnection;
import org.jboss.tools.intellij.openshift.Constants;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.jboss.tools.intellij.openshift.Constants.ODO_CONFIG_YAML;

public class ApplicationTreeModel extends BaseTreeModel<Object> implements ConfigWatcher.Listener, RefreshableTreeModel, LazyMutableTreeNode.ChangeListener, ModuleListener {
    private ApplicationsRootNode ROOT;
    private final Project project;

    private final Map<String, LocalConfig.ComponentSettings> settings = new HashMap();

    public ApplicationTreeModel(Project project) {
        CompletableFuture.runAsync(new ConfigWatcher(new File(ConfigHelper.getKubeConfigPath()), this));
        ROOT = new ApplicationsRootNode(this);
        ROOT.addChangeListener(this);
        this.project = project;
        loadProjectModel(project);
        registerProjectListener(project);
    }

    private void loadProjectModel(Project project) {
        for(Module module : project.getComponent(ModuleManager.class).getModules()) {
            addContext(module.getModuleFile());
        }
    }

    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        /* moduleFile is not yet available when we are notified so try
         * the odo file
         */
        addContext(new File(new File(module.getModuleFilePath()).getParent(), ODO_CONFIG_YAML));
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        removeContext(module.getModuleFile());
    }

    private void addContextToSettings(String path, LocalConfig.ComponentSettings componentSettings) {
        if (!settings.containsKey(path)) {
            settings.put(path, componentSettings);
            refresh();
        }
    }
    private void addContext(File file) {
        if (file.exists()) {
            try {
                LocalConfig config = LocalConfig.load(file.toPath().toUri().toURL());
                addContextToSettings(file.getPath(), config.getComponentSettings());
            } catch (IOException e) {}
        }
    }
    private void addContext(VirtualFile moduleFile) {
        try {
            VirtualFile file = moduleFile.getParent().findFileByRelativePath(ODO_CONFIG_YAML);
            if (file != null && file.isValid()) {
                LocalConfig config = LocalConfig.load(new File(file.getPath()).toPath().toUri().toURL());
                addContextToSettings(moduleFile.getParent().getPath(), config.getComponentSettings());
            }
        } catch (IOException e) { }
    }

    public void addContext(String modulePath) {
        addContext(new File(modulePath, ODO_CONFIG_YAML));
    }

    private void removeContextFromSettings(String path) {
        if (settings.containsKey(path)) {
            settings.remove(path);
            refresh();
        }
    }
    private void removeContext(File file) {
        if (file.exists()) {
            removeContextFromSettings(file.getPath());
        }
    }

    private void removeContext(VirtualFile moduleFile) {
            VirtualFile file = moduleFile.getParent().findFileByRelativePath(ODO_CONFIG_YAML);
            if (file != null && file.isValid()) {
                removeContextFromSettings(moduleFile.getParent().getPath());
            }
    }

    public void removeContext(String modulePath) {
        removeContext(new File(modulePath, ODO_CONFIG_YAML));
    }

    private void registerProjectListener(Project project) {
        MessageBusConnection connection = project.getMessageBus().connect(project);
        connection.subscribe(ProjectTopics.MODULES, this);
    }

    public Map<String, LocalConfig.ComponentSettings> getSettings() {
        return settings;
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

    @Override
    public void onUpdate(ConfigWatcher source) {
        try {
            if (ConfigHelper.isKubeConfigParsable()) {
                ExecHelper.executeAfter(this::refresh, 1, TimeUnit.SECONDS);
            }
        } catch (Exception e) {}
    }

    @Override
    public void refresh() {
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
