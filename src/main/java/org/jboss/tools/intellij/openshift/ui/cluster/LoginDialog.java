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
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends DialogWrapper implements DocumentListener {
  private JPanel contentPane;
  private JTextField userNameField;
  private JPasswordField passwordField;
  private JTextField clusterURLField;
  private JTextField tokenField;

    public LoginDialog(Component parent, String clusterURL) {
    super((Project) null, parent, false, IdeModalityType.IDE);
    init();
    setTitle("Cluster login");
    clusterURLField.setText(clusterURL);
    userNameField.getDocument().addDocumentListener(this);
    passwordField.getDocument().addDocumentListener(this);
    tokenField.getDocument().addDocumentListener(this);
  }


  public static void main(String[] args) {
    LoginDialog dialog = new LoginDialog(null, "");
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

  public String getToken() {
      return tokenField.getText();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    changed(e);
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    changed(e);
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    changed(e);
  }

  public void changed(DocumentEvent e) {
      if (userNameField.getText().trim().length() > 0 || passwordField.getPassword().length > 0) {
        tokenField.setEnabled(false);
      } else {
        tokenField.setEnabled(true);
      }
      if (tokenField.getText().trim().length() > 0) {
        userNameField.setEnabled(false);
        passwordField.setEnabled(false);
      } else {
        userNameField.setEnabled(true);
        passwordField.setEnabled(true);
      }
  }
}
