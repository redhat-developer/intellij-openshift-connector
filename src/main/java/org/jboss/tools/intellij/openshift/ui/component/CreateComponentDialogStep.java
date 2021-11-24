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
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jboss.tools.intellij.openshift.utils.ProjectUtils;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileComponentType;
import org.jboss.tools.intellij.openshift.utils.odo.Starter;

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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"java:S1171","java:S100"})
public class CreateComponentDialogStep extends WizardStep<CreateComponentModel> {
    private JTextField nameTextField;
    private JTextField contextTextField;
    private JTextField applicationTextField;
    private JCheckBox pushAfterCreateCheckBox;
    private JPanel root;
    private JButton browseModulesButton;
    private JButton browseFolderButton;
    private JComboBox<Starter> componentStartersCombo;
    private JLabel informationLabel;
    private JList componentTypeList;
    private final CreateComponentModel model;

    public CreateComponentDialogStep(CreateComponentModel model) {
        super("", "Set the base properties for the component");
        this.model = model;
    }

    private void registerListeners() {
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                model.setName(nameTextField.getText());
                updateState();
            }
        });
        contextTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                model.setContext(contextTextField.getText());
                informationLabel.setVisible(model.isProjectHasDevfile() || model.isProjectIsEmpty());
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
            DevfileComponentType type = (DevfileComponentType) componentTypeList.getSelectedValue();

            if (type == null)
                //Nothing is selected.
                return;

            model.setSelectedComponentType(type);
            componentStartersCombo.setEnabled(model.isProjectIsEmpty());
            List<Starter> starters = Collections.emptyList();
            if (model.isProjectIsEmpty()) {
                try {
                    // TODO: create a loader indicator as shown https://jetbrains.design/intellij/controls/progress_indicators/#05
                    starters = model.getOdo().getComponentTypeInfo(type.getName(), type.getDevfileRegistry().getName()).getStarters();
                } catch (IOException ioException) {
                    starters = Collections.emptyList();
                }
            }
            componentStartersCombo.setModel(new DefaultComboBoxModel(starters.toArray()));
            componentStartersCombo.setRenderer(SimpleListCellRenderer.create("", Starter::getName));
            componentStartersCombo.setSelectedIndex(-1);
            updateState();
        });

        componentStartersCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                model.setSelectedComponentStarter(((Starter) e.getItem()).getName());
            }
        });

        applicationTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                model.setApplication(applicationTextField.getText());
                updateState();
            }
        });
        pushAfterCreateCheckBox.addChangeListener(e -> model.setPushAfterCreate(pushAfterCreateCheckBox.isSelected()));
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
        if (model.getName().length() > 0 && model.getContext().length() > 0 && model.getApplication().length() > 0) {
            state.FINISH.setEnabled(model.isValid());
        } else {
            state.FINISH.setEnabled(false);
        }
    }

    private void loadModel() {
        nameTextField.setText(model.getName());
        contextTextField.setText(model.getContext());
        applicationTextField.setText(model.getApplication());

        DefaultListModel listModel = new DefaultListModel();
        listModel.addAll(model.getComponentTypesList());
        componentTypeList.setModel(listModel);

        pushAfterCreateCheckBox.setSelected(model.isPushAfterCreate());
        if (model.isImportMode()) {
            nameTextField.setEnabled(false);
            String componentType = model.getSelectedComponentType().getName();
            if (componentType != null) {
                componentTypeList.setSelectedValue(componentType, true);
            }
            componentTypeList.setEnabled(false);
            applicationTextField.setEnabled(false);
            pushAfterCreateCheckBox.setSelected(false);
            pushAfterCreateCheckBox.setEnabled(false);
        }
        if (model.isApplicationReadOnly()) {
            applicationTextField.setEnabled(false);
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        root = new JPanel();
        root.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        panel1.add(nameTextField, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Source type");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Context");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contextTextField = new JTextField();
        panel1.add(contextTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Component type");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Component version");
        panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Application");
        panel1.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applicationTextField = new JTextField();
        panel1.add(applicationTextField, new GridConstraints(5, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Push after create");
        panel1.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pushAfterCreateCheckBox = new JCheckBox();
        pushAfterCreateCheckBox.setSelected(true);
        pushAfterCreateCheckBox.setText("");
        panel1.add(pushAfterCreateCheckBox, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browseModulesButton = new JButton();
        browseModulesButton.setText("Select module");
        panel1.add(browseModulesButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browseFolderButton = new JButton();
        browseFolderButton.setText("Select folder");
        panel1.add(browseFolderButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
