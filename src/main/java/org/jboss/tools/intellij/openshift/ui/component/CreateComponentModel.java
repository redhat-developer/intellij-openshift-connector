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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.wizard.WizardModel;
import com.redhat.devtools.alizer.api.LanguageRecognizer;
import com.redhat.devtools.alizer.api.RecognizerFactory;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Predicate;

public class CreateComponentModel extends WizardModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateComponentModel.class);
    private static final LanguageRecognizer recognizer = new RecognizerFactory().createLanguageRecognizer();

    private final Project project;
    private String name = "";
    private String context = "";
    private String application = "app";
    private boolean applicationReadOnly;
    private boolean pushAfterCreate = true;

    private final List<DevfileComponentType> devfileTypes;
    private ComponentType selectedComponentType;

    private boolean importMode;

    private Predicate<String> componentPredicate = x -> false;

    private boolean projectHasDevfile = false;

    private boolean projectIsEmpty = false;

    private String selectedComponentStarter;

    private final Odo odo;

    public CreateComponentModel(String title, Project project, Odo odo, List<DevfileComponentType> types) {
        super(title);
        this.odo = odo;
        this.project = project;
        this.devfileTypes = types;
        VirtualFile file = ProjectUtils.getDefaultDirectory(project);
        if (file != null) {
            setContext(file.getPath());
        }
        add(new CreateComponentDialogStep(this));
    }

    public Project getProject() {
        return project;
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
        if (StringUtils.isEmpty(name)) {
            setName(devfile.getParentFile().getName());
        }
        setSelectedComponentStarter(null);
        if (!projectHasDevfile) {
            try {
                DevfileComponentType type = recognizer.selectDevFileFromTypes(context, devfileTypes);
                if (type != null) {
                    setSelectedComponentType(type);
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public boolean isApplicationReadOnly() {
        return applicationReadOnly;
    }

    public void setApplicationReadOnly(boolean applicationReadOnly) {
        this.applicationReadOnly = applicationReadOnly;
    }

    public boolean isPushAfterCreate() {
        return pushAfterCreate;
    }

    public void setPushAfterCreate(boolean pushAfterCreate) {
        this.pushAfterCreate = pushAfterCreate;
    }

    public ComponentType getSelectedComponentType() {
        return selectedComponentType;
    }

    public void setSelectedComponentType(ComponentType selectedComponentType) {
        this.selectedComponentType = selectedComponentType;
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

    public boolean isValid() {
        return StringUtils.isNotBlank(getName()) && StringUtils.isNotBlank(getApplication()) &&
                StringUtils.isNotBlank(getContext()) &&
                (isProjectHasDevfile() || getSelectedComponentType() != null);
    }

    public boolean hasComponent(String path) {
        return componentPredicate.test(path);
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

    public List<DevfileComponentType> getComponentTypesList() {
        return devfileTypes;
    }
}
