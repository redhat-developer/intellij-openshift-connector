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
package org.jboss.tools.intellij.openshift.ui.service;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public class CreateServiceDialog extends DialogWrapper {
  private JPanel contentPane;
  private JTextField nameField;
  private JComboBox serviceTemplatesComboBox;

  public CreateServiceDialog(Component parent) {
    super(null, false, IdeModalityType.IDE);
    init();
    setTitle("Create service");
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return contentPane;
  }

  public void setServiceTemplates(ServiceTemplate[] serviceTemplates) {
    serviceTemplatesComboBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        value = ((ServiceTemplate) value).getName() + " " + ((ServiceTemplate) value).getPlan();
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
    serviceTemplatesComboBox.setModel(new DefaultComboBoxModel(serviceTemplates));
    serviceTemplatesComboBox.setSelectedIndex(-1);
    serviceTemplatesComboBox.setSelectedIndex(0);
  }

  public String getName() {
    return nameField.getText();
  }

  public ServiceTemplate getServiceTemplate() {
    return (ServiceTemplate) serviceTemplatesComboBox.getSelectedItem();
  }

  @NotNull
  @Override
  protected List<ValidationInfo> doValidateAll() {
    List<ValidationInfo> validations = new ArrayList<>();
    if (nameField.getText().length() == 0) {
      validations.add(new ValidationInfo("Name must be provided", nameField));
    }
    return validations;
  }

  public static void main(String[] args) {
    CreateServiceDialog dialog = new CreateServiceDialog(null);
    dialog.show();
    System.exit(0);
  }

}
