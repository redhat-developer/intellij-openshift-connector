/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends DialogWrapper {
  private JPanel contentPane;
  private JTextField userNameField;
  private JPasswordField passwordField;
  private JTextField clusterURLField;

  public LoginDialog(Component parent, String clusterURL) {
    super((Project) null, parent, false, IdeModalityType.IDE);
    init();
    setTitle("Cluster login");
    clusterURLField.setText(clusterURL);
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
}
