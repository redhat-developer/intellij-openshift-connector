/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.binding;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionListModel;
import org.jboss.tools.intellij.openshift.utils.odo.Binding;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Component;

public class BindingDetailDialog extends DialogWrapper {
    private JPanel contentPane;
    private javax.swing.JTextField serviceName;
    private javax.swing.JTextField bindingName;
    private com.intellij.ui.components.JBList<String> environmentVariables;

    public BindingDetailDialog(Project project, Component parent, Binding binding) {
        super(project, parent, false, IdeModalityType.IDE);
        init();
        setTitle("Binding");
        setBinding(binding);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public void setBinding(Binding binding) {
        bindingName.setText(binding.getName());
        serviceName.setText(binding.getService().getName());
        environmentVariables.setModel(new CollectionListModel<>(binding.getEnvironmentVariables()));
    }
}
