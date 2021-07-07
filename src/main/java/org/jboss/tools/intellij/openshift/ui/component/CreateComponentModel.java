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
import com.redhat.devtools.alizer.api.LanguageRecognizerBuilder;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentKind;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentSourceType;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

public class CreateComponentModel extends WizardModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateComponentModel.class);
    private static LanguageRecognizer recognizer = new LanguageRecognizerBuilder().build();

    private Project project;
    private String name = "";
    private ComponentSourceType sourceType = ComponentSourceType.LOCAL;
    private String context = "";
    private String selectedComponentTypeVersion;
    private String application = "app";
    private boolean applicationReadOnly;
    private boolean pushAfterCreate = true;

    private String gitURL;
    private String gitReference;

    private String binaryFilePath;

    private ComponentType selectedComponentType;

    private boolean importMode;

    private Predicate<String> componentPredicate = x -> false;

    private boolean projectHasDevfile = false;

    private boolean projectIsEmpty = false;

    private String selectedComponentStarter;

    private final DefaultMutableTreeNode top = new DefaultMutableTreeNode("Top");

    private final Odo odo;

    public CreateComponentModel(String title, Project project, Odo odo, List<ComponentType> types) {
        super(title);
        this.odo = odo;
        this.project = project;
        setComponentTypesTree(types);
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

    public ComponentSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ComponentSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getContext() {
        return context;
    }

    protected List<DevfileComponentType> computeComponentTypes() {
        List<DevfileComponentType> types = new ArrayList<>();
        Enumeration<DefaultMutableTreeNode> childs = (Enumeration<DefaultMutableTreeNode>) top.getFirstChild().children();
        while (childs.hasMoreElements()) {
            types.add((DevfileComponentType) childs.nextElement().getUserObject());
        }
        return types;
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
            List<DevfileComponentType> types = computeComponentTypes();
            try {
                DevfileComponentType type = recognizer.selectDevFileFromTypes(context, types);
                if (type != null) {
                    setSelectedComponentType(type);
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    public String getSelectedComponentTypeVersion() {
        return selectedComponentTypeVersion;
    }

    public void setSelectedComponentTypeVersion(String selectedComponentTypeVersion) {
        this.selectedComponentTypeVersion = selectedComponentTypeVersion;
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
                (isProjectHasDevfile() || getSelectedComponentType() != null) &&
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

    private void setComponentTypesTree(List<ComponentType> types) {
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
}
