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
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentMetadata;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CreateComponentModel extends WizardModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateComponentModel.class);
    private final Project project;
    private String name = "";
    private String context = "";
    private boolean devModeAfterCreate = true;

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
                List<ComponentMetadata> types = odo.analyze(context);
                if (!types.isEmpty()) {
                    ComponentMetadata metadata = types.get(0);
                    Optional<DevfileComponentType> type = devfileTypes.stream().filter(t -> t.getDevfileRegistry().getName().equals(metadata.getRegistry()) && t.getName().equals(metadata.getComponentType())).findFirst();
                    if (type.isPresent()) {
                        setSelectedComponentType(type.get());
                    }
                }
            } catch (IOException e) {
                if (!e.getLocalizedMessage().contains("No valid devfile found")) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    public boolean isDevModeAfterCreate() {
        return devModeAfterCreate;
    }

    public void setDevModeAfterCreate(boolean pushAfterCreate) {
        this.devModeAfterCreate = pushAfterCreate;
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
        return StringUtils.isNotBlank(getName()) &&
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
