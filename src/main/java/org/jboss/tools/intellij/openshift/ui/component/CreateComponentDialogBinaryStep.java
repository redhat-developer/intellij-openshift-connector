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

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class CreateComponentDialogBinaryStep extends WizardStep<CreateComponentModel> {
    private JPanel root;
    private JTextField binaryFileTextField;
    private JButton browseButton;
    private final CreateComponentModel model;

    public CreateComponentDialogBinaryStep(CreateComponentModel model) {
        super("Binary file", "Set binary file location");
        this.model = model;
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        state.PREVIOUS.setEnabled(true);
        state.NEXT.setEnabled(false);
        loadModel();
        registerListeners();
        updateState();
        return root;
    }

    private void loadModel() {
        binaryFileTextField.setText(model.getBinaryFilePath());
    }

    private void registerListeners() {
        binaryFileTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                model.setBinaryFilePath(binaryFileTextField.getText());
                updateState();
            }
        });
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(model.getContext());
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(root.getParent()) == JFileChooser.APPROVE_OPTION) {
                binaryFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

    }

    private void updateState() {
        WizardNavigationState state = model.getCurrentNavigationState();
        state.FINISH.setEnabled(model.isValid());
    }



}
