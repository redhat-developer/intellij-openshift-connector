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
package org.jboss.tools.intellij.openshift.ui.component;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;
import java.util.function.Predicate;

public class ModuleSelectionDialog extends DialogWrapper {
  private JPanel contentPane;
  private JList moduleList;

  public ModuleSelectionDialog(Component parent, Project project, Predicate<Module> filter) {
    super(project, parent, false, IdeModalityType.IDE);
    init();
    setTitle("Select Module");
    DefaultListModel model = new DefaultListModel();
    for (Module m : ModuleManager.getInstance(project).getModules()) {
      if (filter.test(m)) {
        model.addElement(m);
      }
    }
    moduleList.setModel(model);
    moduleList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        value = ((Module) value).getName();
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
    if (model.isEmpty()) {
      moduleList.setEnabled(false);
    }
  }

  public Module getSelectedModule() {
    return (Module) moduleList.getSelectedValue();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return contentPane;
  }

}
