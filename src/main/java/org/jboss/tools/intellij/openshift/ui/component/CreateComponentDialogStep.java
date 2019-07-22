package org.jboss.tools.intellij.openshift.ui.component;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;

public class CreateComponentDialogStep extends WizardStep<CreateComponentModel> {
    private JTextField nameTextField;
    private JComboBox sourceTypeComboBox;
    private JTextField contextTextField;
    private JComboBox componentTypeComboBox;
    private JComboBox componentVersionComboBox;
    private JTextField applicationTextField;
    private JCheckBox pushAfterCreateCheckBox;
    private JPanel root;
    private JButton browseModulesButton;
    private JButton browseFolderButton;
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
        sourceTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                model.setSourceType((CreateComponentModel.SourceType) sourceTypeComboBox.getSelectedItem());
            }
        });
        contextTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                model.setContext(contextTextField.getText());
                updateState();
            }
        });
        browseModulesButton.addActionListener(e -> {
            ModuleSelectionDialog dialog = new ModuleSelectionDialog(root.getParent(), model.getProject());
            dialog.show();
            if (dialog.isOK()) {
                contextTextField.setText(dialog.getSelectedModule().getModuleFile().getParent().getPath());
            }
        });
        browseFolderButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(model.getContext());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(root.getParent()) == JFileChooser.APPROVE_OPTION) {
                contextTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        componentTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                value = ((ComponentType) value).getName();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        componentTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                componentVersionComboBox.setModel(new DefaultComboBoxModel(((ComponentType) e.getItem()).getVersions().split(",")));
                componentVersionComboBox.setSelectedIndex(-1);
                componentVersionComboBox.setSelectedIndex(0);
                model.setComponentTypeName(((ComponentType) e.getItem()).getName());
            }
        });
        componentVersionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                model.setComponentTypeVersion((String) e.getItem());
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
        WizardNavigationState state = model.getCurrentNavigationState();
        if (model.getName().length() > 0 && model.getContext().length() > 0 && model.getApplication().length() > 0) {
            state.NEXT.setEnabled(true);
            if (model.getSourceType() == CreateComponentModel.SourceType.LOCAL) {
                state.FINISH.setEnabled(true);
            }
        } else {
            state.NEXT.setEnabled(false);
            state.FINISH.setEnabled(false);
        }
    }

    private void loadModel() {
        nameTextField.setText(model.getName());
        sourceTypeComboBox.setModel(new DefaultComboBoxModel(CreateComponentModel.SourceType.values()));
        sourceTypeComboBox.setSelectedItem(model.getSourceType());
        contextTextField.setText(model.getContext());
        componentTypeComboBox.setModel(new DefaultComboBoxModel(model.getComponentTypes()));
        componentTypeComboBox.setSelectedItem(null);
        componentTypeComboBox.setSelectedItem(Arrays.stream(model.getComponentTypes()).filter(t -> t.getName().equals(model.getComponentTypeName())).findFirst().orElse(model.getComponentTypes()[0]));
        applicationTextField.setText(model.getApplication());
        pushAfterCreateCheckBox.setSelected(model.isPushAfterCreate());
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        state.PREVIOUS.setEnabled(false);
        registerListeners();
        loadModel();
        updateState();
        return root;
    }

    @Override
    public WizardStep onNext(CreateComponentModel model) {
        if (model.getSourceType() == CreateComponentModel.SourceType.GIT) {
            return new CreateComponentDialogGitStep(model);
        } else {
            return WizardStep.FORCED_GOAL_ACHIEVED;
        }
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
        sourceTypeComboBox = new JComboBox();
        panel1.add(sourceTypeComboBox, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Context");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contextTextField = new JTextField();
        panel1.add(contextTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Component type");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        componentTypeComboBox = new JComboBox();
        panel1.add(componentTypeComboBox, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Component version");
        panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        componentVersionComboBox = new JComboBox();
        panel1.add(componentVersionComboBox, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
