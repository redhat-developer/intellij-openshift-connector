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
package org.jboss.tools.intellij.openshift.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardModel;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentSourceType;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Predicate;

public class CreateComponentModel extends WizardModel {
    private Project project;
    private String name = "";
    private ComponentSourceType sourceType = ComponentSourceType.LOCAL;
    private String context = "";
    private String componentTypeName;
    private String componentTypeVersion;
    private String application = "";
    private boolean pushAfterCreate = true;

    private String gitURL;
    private String gitReference;

    private String binaryFilePath;

    private ComponentKind componentKind;

    private boolean importMode;

    private Predicate<String> componentPredicate = x -> false;

    private boolean projectHasDevfile = false;

    private boolean projectIsEmpty = false;

    private String selectedComponentStarter;

    private final DefaultMutableTreeNode top = new DefaultMutableTreeNode("Top");

    private final Odo odo;

    private String registryName;

    public CreateComponentModel(String title, Odo odo) {
        super(title);
        this.odo = odo;
        add(new CreateComponentDialogStep(this));
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isProjectHasDevfile() {
        return projectHasDevfile;
    }

    public boolean isProjectIsEmpty() {
        return projectIsEmpty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ComponentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
        File devfile = new File(context, Constants.DEVFILE_NAME);
        projectHasDevfile = Files.exists(devfile.toPath());
        if (!context.isEmpty()) {
            projectIsEmpty = ProjectUtils.isEmpty(new File(context));
        }else {
            projectIsEmpty = false;
        }
        setSelectedComponentStarter(null);
    }

    public String getComponentTypeName() {
        return componentTypeName;
    }

    public void setComponentTypeName(String componentTypeName) {
        this.componentTypeName = componentTypeName;
    }

    public String getComponentTypeVersion() {
        return componentTypeVersion;
    }

    public void setComponentTypeVersion(String componentTypeVersion) {
        this.componentTypeVersion = componentTypeVersion;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public boolean isPushAfterCreate() {
        return pushAfterCreate;
    }

    public void setPushAfterCreate(boolean pushAfterCreate) {
        this.pushAfterCreate = pushAfterCreate;
    }

    public String getGitURL() {
        return gitURL;
    }

    public void setGitURL(String gitURL) {
        this.gitURL = gitURL;
    }

    public String getGitReference() {
        return gitReference;
    }

    public void setGitReference(String gitReference) {
        this.gitReference = gitReference;
    }

    public String getBinaryFilePath() {
        return binaryFilePath;
    }

    public void setBinaryFilePath(String binaryFilePath) {
        this.binaryFilePath = binaryFilePath;
    }

    public ComponentKind getComponentKind() {
        return componentKind;
    }

    public void setComponentKind(ComponentKind componentKind) {
        this.componentKind = componentKind;
    }

    public boolean isImportMode() {
        return importMode;
    }

    public void setImportMode(boolean importMode) {
        this.importMode = importMode;
    }

    public void setComponentPredicate(Predicate<String> componentPredicate) {
        this.componentPredicate = componentPredicate;
    }

    protected boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(getName()) && StringUtils.isNotBlank(getApplication()) &&
                StringUtils.isNotBlank(getContext()) &&
                (isProjectHasDevfile() || StringUtils.isNotBlank(getComponentTypeName())) &&
                (getSourceType() == ComponentSourceType.LOCAL ||
                        (getSourceType() == ComponentSourceType.GIT && StringUtils.isNotBlank(getGitURL()) && isValidURL(getGitURL())) ||
                        (getSourceType() == ComponentSourceType.BINARY && StringUtils.isNotBlank(getBinaryFilePath())));
    }

    public boolean hasComponent(String path) {
        return componentPredicate.test(path);
    }

    public TreeNode getComponentTypesTree() {
        return top;
    }

    public void setComponentTypesTree(List<ComponentType> types) {
        // creates the default Roots
        DefaultMutableTreeNode devfileComponents = new DefaultMutableTreeNode("DevFile Components");
        DefaultMutableTreeNode s2iComponents = new DefaultMutableTreeNode("S2I Components (Deprecated)");
        for (ComponentType type : types) {
            switch (type.getKind()) {
                case S2I:
                    s2iComponents.add(new DefaultMutableTreeNode(type, false));
                    break;
                case DEVFILE:
                    devfileComponents.add(new DefaultMutableTreeNode(type, false));
                    break;
            }
        }
        top.add(devfileComponents);
        top.add(s2iComponents);
    }

    public Odo getOdo() {
        return this.odo;
    }

    public String getSelectedComponentStarter() {
        return selectedComponentStarter;
    }

    public void setSelectedComponentStarter(String selectedComponentStarter) {
        this.selectedComponentStarter = selectedComponentStarter;
    }

    public String getDevFileRegistryName() {
        if (getComponentKind().equals(ComponentKind.DEVFILE)){
            return registryName;
        }
        return null;
    }

    public void setDevFileRegistryName(String registryName) {
        this.registryName = registryName;
    }
}
