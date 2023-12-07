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
package org.jboss.tools.intellij.openshift.ui.cluster;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.HTMLEditorKitBuilder;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.ui.sandbox.SandboxDialog;
import org.jboss.tools.intellij.openshift.ui.sandbox.SandboxModel;
import org.jboss.tools.intellij.openshift.utils.OCCommandUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"java:S1171", "java:S100"})
public class LoginDialog extends DialogWrapper implements DocumentListener {
    private final Project project;
    private JPanel contentPane;
    private JTextField userNameField;
    private JPasswordField passwordField;
    private JTextField clusterURLField;
    private JPasswordField tokenField;
    private JButton pasteLoginCommandButton;
    private JEditorPane labelEditorPane;

    public LoginDialog(Project project, Component parent, String clusterURL) {
        super(project, parent, false, IdeModalityType.IDE);
        this.project = project;
        init();
        setTitle("Cluster login");
        clusterURLField.setText(clusterURL);
        userNameField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        tokenField.getDocument().addDocumentListener(this);
        pasteLoginCommandButton.addActionListener(e -> parseLoginCommandFromClipboard());
        labelEditorPane.setEditorKit(new HTMLEditorKitBuilder().build());
        labelEditorPane.setText("Enter the cluster URL and the required credentials. You can also bootstrap a <a href=''>Red Hat Developer Sandbox</a> cluster using your Red Hat account");
        labelEditorPane.addHyperlinkListener(this::loginToSandbox);
    }

    private void loginToSandbox(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            TelemetrySender telemetrySender = new TelemetrySender(TelemetryService.DEVSANDBOX_LOGIN_DIALOG);
            SandboxModel sandboxModel = new SandboxModel("Red Hat Developer Sandbox", project, telemetrySender);
            SandboxDialog dialog1 = new SandboxDialog(project, true, sandboxModel);
            dialog1.show();
            if (sandboxModel.isComplete()) {
                telemetrySender.sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
                clusterURLField.setText(sandboxModel.getClusterURL());
                tokenField.setText(sandboxModel.getClusterToken());
            } else {
                telemetrySender.sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
            }
        }
    }

    private void parseLoginCommandFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String clipboardText = null;
        try {
            clipboardText = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Login Command"));
        }
        if (clipboardText == null) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Cannot paste clipboard into the login dialog. Only text is accepted.", "Error when parsing login command"));
        } else {
            if (OCCommandUtils.isValidCommand(clipboardText)) {
                String server = OCCommandUtils.getServer(clipboardText);
                clusterURLField.setText(server);
                String token = OCCommandUtils.getToken(clipboardText);
                if (token != null && !token.isEmpty()) {
                    tokenField.setText(token);
                } else {
                    String username = OCCommandUtils.getUsername(clipboardText);
                    if (username != null && !username.isEmpty()) {
                        userNameField.setText(username);
                    }
                    String password = OCCommandUtils.getPassword(clipboardText);
                    if (password != null && !password.isEmpty()) {
                        passwordField.setText(password);
                    }
                }
            } else {
                String message = "Login command pasted from clipboard is not valid:\n" + clipboardText;
                UIHelper.executeInUI(() -> Messages.showErrorDialog(message, "Error when parsing login command"));
            }
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getUserName() {
        return userNameField.getText();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public String getClusterURL() {
        return clusterURLField.getText();
    }

    public char[] getToken() {
        return tokenField.getPassword();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changed();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changed();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changed();
    }

    public void changed() {
        if (getUserName().trim().length() > 0 || getPassword().length > 0) {
            tokenField.setEnabled(false);
        } else {
            tokenField.setEnabled(true);
        }
        if (getToken().length > 0) {
            userNameField.setEnabled(false);
            passwordField.setEnabled(false);
        } else {
            userNameField.setEnabled(true);
            passwordField.setEnabled(true);
        }
    }

    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();
        if (tokenField.isEnabled() && getToken().length == 0) {
            validations.add(new ValidationInfo("Specify the token.", tokenField));
        }
        if (userNameField.isEnabled() && getUserName().trim().isEmpty()) {
            validations.add(new ValidationInfo("Specify the username.", userNameField));
        }
        if (passwordField.isEnabled() && getPassword().length == 0) {
            validations.add(new ValidationInfo("Specify the password.", passwordField));
        }
        if (getClusterURL().isEmpty()) {
            validations.add(new ValidationInfo("Specify the cluster URL.", clusterURLField));
        }
        if (!getClusterURL().matches("^https?[a-zA-Z0-9:/.-]+")) {
            validations.add(new ValidationInfo("\"" + getClusterURL() + "\" is not an allowed URL value.", clusterURLField));
        }
        return validations;
    }

}
