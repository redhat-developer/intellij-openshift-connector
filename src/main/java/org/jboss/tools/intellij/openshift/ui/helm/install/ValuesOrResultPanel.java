/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.helm.install;

import com.intellij.ide.plugins.MultiPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

class ValuesOrResultPanel extends MultiPanel {
  private static final int LOADING = 0;
  private static final int CHART_VALUES = 1;
  private static final int INSTALL_RESULT = 2;
  private final Disposable parentDisposable;
  private final Project project;

  ValuesOrResultPanel(Disposable parentDisposable, Project project) {
    this.parentDisposable = parentDisposable;
    this.project = project;
  }

  void showInstallResult(String output) {
    ResultPanel panel = showPanel(INSTALL_RESULT, ResultPanel.class);
    if (panel != null) {
      panel.showOutput(output);
    }
  }

  void showInstallError(String error) {
    ResultPanel panel = showPanel(INSTALL_RESULT, ResultPanel.class);
    if (panel != null) {
      panel.showError(error);
    }
  }

  void showChartValues(String values) {
    ValuesEditor panel = showPanel(CHART_VALUES, ValuesEditor.class);
    if (panel != null) {
      panel.showValues(values);
    }
  }

  void showLoading() {
    select(LOADING, true);
  }

  String getChartValues() {
    String values = null;
    ValuesEditor panel = getPanel(CHART_VALUES, ValuesEditor.class);
    if (panel != null) {
      values = panel.getValues();
    }
    return values;
  }

  void setValuesListener(DocumentListener listener) {
    ValuesEditor panel = getPanel(CHART_VALUES, ValuesEditor.class);
    if (panel == null){
      return;
    }
    panel.setValuesListener(listener);
  }

  @Override
  protected JComponent create(Integer key) {
    if (key == LOADING) {
      return new LoadingPanel();
    } else if (key == INSTALL_RESULT) {
      return new ResultPanel();
    } else if (key == CHART_VALUES) {
      return new ValuesEditor(parentDisposable, project);
    } else {
      return null;
    }
  }

  private <T extends JComponent> T showPanel(int key, Class<T> clazz) {
    T panel = getPanel(key, clazz);
    select(key, true);
    return panel;
  }

  @Nullable
  private <T extends JComponent> T getPanel(int key, Class<T> clazz) {
    JComponent component = getValue(key, true);
    if (component != null
      && component.getClass().equals(clazz)) {
      return (T) component;
    } else {
      return null;
    }
  }
}