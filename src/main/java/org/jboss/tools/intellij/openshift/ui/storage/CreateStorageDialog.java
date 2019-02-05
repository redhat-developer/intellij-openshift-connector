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
package org.jboss.tools.intellij.openshift.ui.storage;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class CreateStorageDialog extends DialogWrapper {
  private JPanel contentPane;
  private JTextField nameTextField;
  private JComboBox sizeComboBox;
  private JTextField mountPathTextField;

  public CreateStorageDialog(Component parent) {
    super(null, parent, false, IdeModalityType.IDE);
    init();
    setTitle("Create storage");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return contentPane;
  }

  @NotNull
  @Override
  protected List<ValidationInfo> doValidateAll() {
    List<ValidationInfo> validations = new ArrayList<>();
    if (nameTextField.getText().length() == 0) {
      validations.add(new ValidationInfo("Name must be provided", nameTextField));
    }
    if (mountPathTextField.getText().length() == 0) {
      validations.add(new ValidationInfo("Mount path must be provided", mountPathTextField));
    }
    return validations;
  }

  public String getName() {
    return nameTextField.getText();
  }

  public String getMountPath() {
    return mountPathTextField.getText();
  }

  public String getStorageSize() {
    return (String) sizeComboBox.getSelectedItem();
  }

  public static void main(String[] args) {
    CreateStorageDialog dialog = new CreateStorageDialog(null);
    dialog.show();
    System.exit(0);
  }

}
