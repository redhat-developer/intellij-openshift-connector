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
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Insets;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"java:S1171","java:S100"})
public class CreateURLDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField nameTextField;
    private JCheckBox secureCheckBox;
    private JTextField portField;
    private JTextField hostField;
    private JLabel hostLabel;

    /**
     * Constructor for CreateURL dialog.
     *
     * @param showHostField set to @true to show editable hostField.
     */
    public CreateURLDialog(boolean showHostField, String hostValue) {
        super(null, false, IdeModalityType.IDE);
        init();
        setTitle("Create URL");
        portField.setText("1024");
        hostField.setVisible(showHostField);
        hostLabel.setVisible(showHostField);
        if (showHostField) {
            hostField.setText(hostValue);
        }
    }

    private String getPortFieldValue() {
        return portField.getText().trim();
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();
        if (portField.isVisible()) {
            try {
                Integer value = Integer.valueOf(getPortFieldValue());
                if (!ValueRange.of(1024, 65535).isValidIntValue(value)) {
                    validations.add(new ValidationInfo("Port value must be between 1024 and 65535", portField));
                }
            } catch (NumberFormatException e) {
                validations.add(new ValidationInfo("Port value must be an integer", portField));
            }
        }
        return validations;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getName() {
        return nameTextField.getText();
    }

    public boolean isSecure() {
        return secureCheckBox.isSelected();
    }

    public Integer getPort() {
        return Integer.valueOf(getPortFieldValue());
    }

    public String getHost() {
        return hostField.getText().trim();
    }

}
