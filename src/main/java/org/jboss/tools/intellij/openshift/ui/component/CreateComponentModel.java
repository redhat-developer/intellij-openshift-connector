package org.jboss.tools.intellij.openshift.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardModel;
import org.apache.commons.lang.StringUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;

import java.net.MalformedURLException;
import java.net.URL;

public class CreateComponentModel extends WizardModel {
    public enum SourceType {
        LOCAL("Local"),
        GIT("Git"),
        BINARY("Binary");

        private final String label;

        SourceType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    };
    private Project project;
    private String name = "";
    private SourceType sourceType = SourceType.LOCAL;
    private String context = "";
    private ComponentType[] componentTypes;
    private String componentTypeName;
    private String componentTypeVersion;
    private String application = "";
    private boolean pushAfterCreate = true;

    private String gitURL;
    private String gitReference;

    private String binaryFilePath;

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

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
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
                (getSourceType() == SourceType.LOCAL ||
                        (getSourceType() == SourceType.GIT && StringUtils.isNotBlank(getGitURL()) && isValidURL(getGitURL())) ||
                        (getSourceType() == SourceType.BINARY && StringUtils.isNotBlank(getBinaryFilePath())));
    }
}
