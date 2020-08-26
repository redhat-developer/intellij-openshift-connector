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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardModel;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentSourceType;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;

public class CreateComponentModel extends WizardModel {
    private Project project;
    private String name = "";
    private ComponentSourceType sourceType = ComponentSourceType.LOCAL;
    private String context = "";
    private ComponentType[] componentTypes;
    private String componentTypeName;
    private String componentTypeVersion;
    private String application = "";
    private boolean pushAfterCreate = true;

    private String gitURL;
    private String gitReference;

    private String binaryFilePath;

    private boolean importMode;

    private Predicate<String> componentPredicate = x -> false;

    public CreateComponentModel(String title) {
        super(title);
        add(new CreateComponentDialogStep(this));
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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
    }

    public ComponentType[] getComponentTypes() {
        return componentTypes;
    }

    public void setComponentTypes(ComponentType[] componentTypes) {
        this.componentTypes = componentTypes;
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

    public boolean isImportMode() {
        return importMode;
    }

    public void setImportMode(boolean importMode) {
        this.importMode = importMode;
    }

    public Predicate<String> getComponentPredicate() {
        return componentPredicate;
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
                (getSourceType() == ComponentSourceType.LOCAL ||
                        (getSourceType() == ComponentSourceType.GIT && StringUtils.isNotBlank(getGitURL()) && isValidURL(getGitURL())) ||
                        (getSourceType() == ComponentSourceType.BINARY && StringUtils.isNotBlank(getBinaryFilePath())));
    }

    public boolean hasComponent(String path) {
        return componentPredicate.test(path);
    }
}
