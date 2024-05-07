/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.helm;

import com.intellij.ide.plugins.MultiPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import java.awt.Component;
import java.util.stream.Stream;
import javax.swing.JComponent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.helm.install.InstallPanel;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

public class InstallOrDetailsPanels extends MultiPanel {

  public static final int DETAILS_PANEL = 0;
  public static final int INSTALL_PANEL = 1;

  private final ApplicationsRootNode rootNode;
  private final Helm helm;
  private final Odo odo;
  private final Project project;
  private ChartVersions chart;
  private final Disposable disposable = Disposer.newDisposable();

  InstallOrDetailsPanels(ApplicationsRootNode rootNode, Disposable parentDisposable, Helm helm, Odo odo, Project project) {
    this.rootNode = rootNode;
    this.helm = helm;
    this.odo = odo;
    this.project = project;
    Disposer.register(parentDisposable, disposable);
  }

  @Override
  protected JComponent create(Integer key) {
    if (key == DETAILS_PANEL) {
      return new DetailsPanel(chart, disposable, this);
    } else if (key == INSTALL_PANEL) {
      return new InstallPanel(chart, rootNode, disposable, helm, odo, project);
    } else {
      return null;
    }
  }

  public void setChart(ChartVersions chart) {
    this.chart = chart;
    setCharts(getComponents());
    select(DETAILS_PANEL, false);
  }

  @Override
  public void dispose() {
    disposable.dispose();
  }

  private void setCharts(Component[] components) {
    Stream.of(components).forEach(component -> {
      if (component instanceof ChartPanel) {
        ((ChartPanel) component).setChart(chart);
      }
    });
  }
}
