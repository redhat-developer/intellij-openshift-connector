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
package org.jboss.tools.intellij.openshift.ui.url;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.util.List;

public class CreateURLDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField nameTextField;
    private JComboBox portsComboBox;

    public CreateURLDialog(Component parent) {
        super(null, false, IdeModalityType.IDE);
        init();
        setTitle("Create URL");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getName() {
        return nameTextField.getText();
    }

    public void setPorts(List<Integer> ports) {
        portsComboBox.setModel(new CollectionComboBoxModel(ports));
        portsComboBox.setSelectedIndex(-1);
        portsComboBox.setSelectedIndex(0);
    }

    public Integer getSelectedPort() {
        return (Integer) portsComboBox.getSelectedItem();
    }

    public static void main(String[] args) {
        CreateURLDialog dialog = new CreateURLDialog(null);
        dialog.show();
        System.exit(0);
    }
}
