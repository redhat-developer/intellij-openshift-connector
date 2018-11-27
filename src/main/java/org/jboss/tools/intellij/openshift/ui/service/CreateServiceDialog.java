package org.jboss.tools.intellij.openshift.ui.service;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jboss.tools.intellij.openshift.utils.OdoHelper;
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

  public void setServiceTemplates(OdoHelper.ServiceTemplate[] serviceTemplates) {
    serviceTemplatesComboBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        value = ((OdoHelper.ServiceTemplate) value).getName() + " " + ((OdoHelper.ServiceTemplate) value).getPlan();
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

  public OdoHelper.ServiceTemplate getServiceTemplate() {
    return (OdoHelper.ServiceTemplate) serviceTemplatesComboBox.getSelectedItem();
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
