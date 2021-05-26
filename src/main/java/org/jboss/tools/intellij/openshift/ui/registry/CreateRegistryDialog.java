package org.jboss.tools.intellij.openshift.ui.registry;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CreateRegistryDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField nameTextField;
    private JTextField urlTextField;
    private JTextField tokenTextField;

    public CreateRegistryDialog() {
        super(null, false, IdeModalityType.IDE);
        init();
        setTitle("Create registry");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    public String getName() {
        return nameTextField.getText();
    }

    public String getURL() {
        return urlTextField.getText();
    }

    public String getToken() {
        return tokenTextField.getText();
    }
}
