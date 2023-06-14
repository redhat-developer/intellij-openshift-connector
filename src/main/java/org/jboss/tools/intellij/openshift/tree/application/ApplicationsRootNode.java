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

import com.intellij.ProjectTopics;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentDescriptor;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCliFactory;
import org.jboss.tools.intellij.openshift.utils.odo.OdoProjectDecorator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.Constants.GROUP_DISPLAY_ID;

public class ApplicationsRootNode implements ModuleListener, ConfigWatcher.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsRootNode.class);
    private final Project project;
    private final ApplicationsTreeStructure structure;
    private Odo odo;
    private boolean logged;
    private Config config;

    private final Map<String, ComponentDescriptor> components = new HashMap<>();

    public ApplicationsRootNode(Project project, ApplicationsTreeStructure structure) {
        this.project = project;
        this.structure = structure;
        initConfigWatcher();
        config = loadConfig();
        registerProjectListener(project);
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public CompletableFuture<Odo> initializeOdo() {
        return OdoCliFactory.getInstance().getOdo(project).whenComplete((odo, err) -> {
            if (this.odo != null) {
                this.odo.release();
            }
            this.odo = new OdoProjectDecorator(odo, this);
            loadProjectModel(project);
        });
    }

    public Odo getOdo() {
        return odo;
    }

    public Project getProject() {
        return project;
    }

    protected void initConfigWatcher() {
        ExecHelper.submit(new ConfigWatcher(Paths.get(ConfigHelper.getKubeConfigPath()), this));
    }

    protected Config loadConfig() {
        return ConfigHelper.safeLoadKubeConfig();
    }

    public Map<String, ComponentDescriptor> getComponents() {
        return components;
    }

    protected void loadProjectModel(Project project) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            addContext(ProjectUtils.getModuleRoot(module));
        }
    }

    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        addContext(ProjectUtils.getModuleRoot(module));
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        removeContext(ProjectUtils.getModuleRoot(module));
    }

    private void addContextToSettings(String path, ComponentDescriptor descriptor) {
        if (!components.containsKey(path)) {
            if (descriptor.isPreOdo3()) {
                getOdo().migrateComponent(path, descriptor.getName());
                Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Component migration",
                        "The component " + descriptor.getName() + " has been migrated to odo 3.x",
                        NotificationType.INFORMATION), project);
            }
            components.put(path, descriptor);
        }
    }

    private void addContext(VirtualFile modulePathFile) {
        if (modulePathFile != null && modulePathFile.isValid() && getOdo() != null) {
            try {
                List<ComponentDescriptor> descriptors = getOdo().discover(modulePathFile.toNioPath().toString());
                descriptors.forEach(descriptor ->
                        addContextToSettings(descriptor.getPath(), descriptor)
                );
            } catch (IOException ex) {
                //filter out some common exception when no logged or no authorizations
                if (doNotLogFromMessage(ex.getMessage())) {
                    LOGGER.error(ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    private static boolean doNotLogFromMessage(String message) {
        return !(message.contains("Unauthorized") ||
                message.contains("unable to access the cluster: servicebindings.binding.operators.coreos.com") ||
                message.contains("the server has asked for the client to provide credentials"));
    }

    public void addContext(String modulePath) {
        addContext(LocalFileSystem.getInstance().refreshAndFindFileByPath(modulePath));
    }

    private void removeContextFromSettings(String modulePath) {
        if (components.containsKey(modulePath)) {
            components.remove(modulePath);
            structure.fireModified(this);
        }
    }

    public void removeContext(File file) {
        if (file.exists()) {
            removeContextFromSettings(file.getPath());
        }
    }

    private void removeContext(VirtualFile modulePathFile) {
        removeContextFromSettings(modulePathFile.getPath());
    }

    protected void registerProjectListener(Project project) {
        MessageBusConnection connection = project.getMessageBus().connect(project);
        connection.subscribe(ProjectTopics.MODULES, this);
    }

    @Override
    public void onUpdate(ConfigWatcher source, Config config) {
        if (hasContextChanged(config, this.config)) {
            refresh();
        }
        this.config = config;
    }

    private boolean hasContextChanged(Config newConfig, Config currentConfig) {
        NamedContext currentContext = KubeConfigUtils.getCurrentContext(currentConfig);
        NamedContext newContext = KubeConfigUtils.getCurrentContext(newConfig);
        return hasServerChanged(newContext, currentContext)
                || hasNewToken(newContext, newConfig, currentContext, currentConfig);
    }

    private boolean hasServerChanged(NamedContext newContext, NamedContext currentContext) {
        return newContext == null
                || currentContext == null
                || !StringUtils.equals(currentContext.getContext().getCluster(), newContext.getContext().getCluster())
                || !StringUtils.equals(currentContext.getContext().getUser(), newContext.getContext().getUser())
                || !StringUtils.equals(currentContext.getContext().getNamespace(), newContext.getContext().getNamespace());
    }

    private boolean hasNewToken(NamedContext newContext, Config newConfig, NamedContext currentContext, Config currentConfig) {
        if (newContext == null) {
            return false;
        }
        if (currentContext == null) {
            return true;
        }
        String newToken = KubeConfigUtils.getUserToken(newConfig, newContext.getContext());
        if (newToken == null) {
            // logout, do not refresh, LogoutAction already refreshes
            return false;
        }
        String currentToken = KubeConfigUtils.getUserToken(currentConfig, currentContext.getContext());
        return !StringUtils.equals(newToken, currentToken);
    }

    public void refresh() {
        OdoCliFactory.getInstance().resetOdo();
        initializeOdo().thenAccept(odo -> structure.fireModified(this));
    }

    public ApplicationsTreeStructure getStructure() {
        return structure;
    }
}
