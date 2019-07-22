package org.jboss.tools.intellij.openshift.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardDialog;

public class CreateComponentDialog extends WizardDialog<CreateComponentModel> {
    public CreateComponentDialog(Project project, boolean canBeParent, CreateComponentModel model) {
        super(project, canBeParent, model);
    }
}
