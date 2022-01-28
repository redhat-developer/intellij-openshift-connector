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
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBHtmlEditorKit;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.ui.sandbox.SandboxDialog;
import org.jboss.tools.intellij.openshift.ui.sandbox.SandboxModel;
import org.jboss.tools.intellij.openshift.utils.OCCommandUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

@SuppressWarnings({"java:S1171","java:S100"})
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
        labelEditorPane.setEditorKit(new JBHtmlEditorKit());
        labelEditorPane.setText("Enter the cluster URL and the required credentials. You can also bootstrap a <a href=''>Red Hat Developer Sandbox</a> cluster using your Red Hat account");
        labelEditorPane.addHyperlinkListener(this::loginToSandbox);
    }

    @NotNull
    private void loginToSandbox(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                SandboxModel sandboxModel = new SandboxModel("Red Hat Developer Sandbox", project);
                SandboxDialog dialog1 = new SandboxDialog(project, true, sandboxModel);
                dialog1.show();
                if (sandboxModel.isComplete()) {
                    clusterURLField.setText(sandboxModel.getClusterURL());
                    tokenField.setText(sandboxModel.getClusterToken());
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

    public static void main(String[] args) {
        LoginDialog dialog = new LoginDialog(null, null, "");
        dialog.pack();
        dialog.show();
        System.exit(0);
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(500, 198));
        contentPane.setPreferredSize(new Dimension(500, 198));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Cluster URL");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Password:");
        panel1.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Username:");
        panel1.add(label3, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userNameField = new JTextField();
        panel1.add(userNameField, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        clusterURLField = new JTextField();
        panel1.add(clusterURLField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordField = new JPasswordField();
        panel1.add(passwordField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Token");
        panel1.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tokenField = new JPasswordField();
        panel1.add(tokenField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
