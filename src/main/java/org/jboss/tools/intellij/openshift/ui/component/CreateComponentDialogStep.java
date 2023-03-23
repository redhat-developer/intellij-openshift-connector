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

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Starter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings({"java:S1171", "java:S100"})
public class CreateComponentDialogStep extends WizardStep<CreateComponentModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateComponentDialogStep.class);
    private JTextField nameTextField;
    private JTextField contextTextField;
    private JCheckBox devModeAfterCreateCheckBox;
    private JPanel root;
    private JButton browseModulesButton;
    private JButton browseFolderButton;
    private JComboBox<Starter> componentStartersCombo;
    private JLabel informationLabel;
    private JList<DevfileComponentType> componentTypeList;
    private final CreateComponentModel model;

    public CreateComponentDialogStep(CreateComponentModel model) {
        super("", "Set the base properties for the component");
        this.model = model;
    }

    private void registerListeners() {
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                model.setName(nameTextField.getText());
                updateState();
            }
        });
        contextTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                model.setContext(contextTextField.getText());
                informationLabel.setVisible(model.isProjectHasDevfile() || model.isProjectIsEmpty());
                componentTypeList.setEnabled(!model.isProjectHasDevfile());
                componentStartersCombo.setEnabled(model.isProjectIsEmpty());
                componentStartersCombo.removeAllItems();
                updateState();
            }
        });
        browseModulesButton.addActionListener(e -> {
            ModuleSelectionDialog dialog = new ModuleSelectionDialog(root.getParent(), model.getProject(), m -> !model.hasComponent(VfsUtilCore.virtualToIoFile(ProjectUtils.getModuleRoot(m)).getAbsolutePath()));
            dialog.show();
            if (dialog.isOK()) {
                contextTextField.setText(ProjectUtils.getModuleRoot(dialog.getSelectedModule()).getPath());
            }
            updateState();
        });
        browseFolderButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(model.getContext());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return !model.hasComponent(f.getPath());
                }

                @Override
                public String getDescription() {
                    return "Folders not containing Odo components";
                }
            });
            if (chooser.showOpenDialog(root.getParent()) == JFileChooser.APPROVE_OPTION) {
                contextTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
            updateState();
        });

        componentTypeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        componentTypeList.addListSelectionListener(e -> {
            DevfileComponentType type = componentTypeList.getSelectedValue();

            if (type == null)
                //Nothing is selected.
                return;

            model.setSelectedComponentType(type);
            componentStartersCombo.setEnabled(model.isProjectIsEmpty());
            DefaultComboBoxModel<Starter> jCombBoxModel  = new DefaultComboBoxModel<>();
            if (model.isProjectIsEmpty()) {
                try {
                    // TODO: create a loader indicator as shown https://jetbrains.design/intellij/controls/progress_indicators/#05
                    List<Starter> starters = model.getOdo().getComponentTypeInfo(type.getName(), type.getDevfileRegistry().getName()).getStarters();
                    jCombBoxModel.addAll(starters);
                } catch (IOException ioe) {
                    LOGGER.error(ioe.getLocalizedMessage(), e);
                }
            }
            componentStartersCombo.setModel(jCombBoxModel);
            componentStartersCombo.setRenderer(SimpleListCellRenderer.create("", Starter::getName));
            componentStartersCombo.setSelectedIndex(-1);
            updateState();
        });

        componentStartersCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                model.setSelectedComponentStarter(((Starter) e.getItem()).getName());
            }
        });

        devModeAfterCreateCheckBox.addChangeListener(e -> model.setDevModeAfterCreate(devModeAfterCreateCheckBox.isSelected()));
    }

    private void updateState() {
        // update informationLabel
        if (model.isProjectIsEmpty()) {
            informationLabel.setText("Context is empty, you can initialize it from starters (templates).");
        }
        if (model.isProjectHasDevfile()) {
            informationLabel.setText("Context already has a devfile, component type selection is not required.");
        }
        ComponentType type = model.getSelectedComponentType();
        if (type != null) {
            componentTypeList.setSelectedValue(type, true);
        } else {
            componentTypeList.getSelectionModel().clearSelection();
        }
        WizardNavigationState state = model.getCurrentNavigationState();
        if (model.getName().length() > 0 && model.getContext().length() > 0) {
            state.FINISH.setEnabled(model.isValid());
        } else {
            state.FINISH.setEnabled(false);
        }
    }

    private void loadModel() {
        nameTextField.setText(model.getName());
        contextTextField.setText(model.getContext());

        DefaultListModel<DevfileComponentType> listModel = new DefaultListModel<>();
        listModel.addAll(model.getComponentTypesList());
        componentTypeList.setModel(listModel);

        devModeAfterCreateCheckBox.setSelected(model.isDevModeAfterCreate());
        if (model.isImportMode()) {
            nameTextField.setEnabled(false);
            String componentType = model.getSelectedComponentType().getName();
            if (componentType != null) {
                componentTypeList.setSelectedValue(componentType, true);
            }
            componentTypeList.setEnabled(false);
            devModeAfterCreateCheckBox.setSelected(false);
            devModeAfterCreateCheckBox.setEnabled(false);
        }
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        state.PREVIOUS.setEnabled(false);
        registerListeners();
        loadModel();
        updateState();
        return root;
    }

}
