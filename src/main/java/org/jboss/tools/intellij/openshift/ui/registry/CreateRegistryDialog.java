package org.jboss.tools.intellij.openshift.ui.registry;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import org.jboss.tools.intellij.openshift.utils.odo.DevfileRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.util.List;

public class CreateRegistryDialog extends DialogWrapper {
    private final List<DevfileRegistry> registries;
    private JPanel contentPane;
    private JTextField nameTextField;
    private JTextField urlTextField;
    private JTextField tokenTextField;

    public CreateRegistryDialog(List<DevfileRegistry> registries) {
        super(null, false, IdeModalityType.IDE);
        init();
        setTitle("Create registry");
        DocumentAdapter adapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                validate();
            }
        };
        nameTextField.getDocument().addDocumentListener(adapter);
        urlTextField.getDocument().addDocumentListener(adapter);
        this.registries = registries;
        validate();
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

    @Override
    public void validate() {
        super.validate();
        if (nameTextField.getText().isEmpty()) {
            setErrorText("Name cannot be empty", nameTextField);
            setOKActionEnabled(false);
        } else if (registries.stream().anyMatch(registry -> registry.getName().equals(nameTextField.getText()))) {
            setErrorText("Name is already used", nameTextField);
            setOKActionEnabled(false);
        } else if (urlTextField.getText().isEmpty()) {
            setErrorText("Registry url cannot be empty", urlTextField);
            setOKActionEnabled(false);
        } else if (registries.stream().anyMatch(registry -> registry.getURL().equals(urlTextField.getText()))) {
            setErrorText("Registry URL is already configured", urlTextField);
            setOKActionEnabled(false);
        } else {
            setErrorText(null);
            setOKActionEnabled(true);
        }
    }
}
