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
import com.intellij.ui.wizard.WizardDialog;

public class CreateComponentDialog extends WizardDialog<CreateComponentModel> {
    public CreateComponentDialog(Project project, boolean canBeParent, CreateComponentModel model) {
        super(project, canBeParent, model);
    }
}
