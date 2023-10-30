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
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import java.awt.Component;

public class ChartPanels extends MultiPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChartPanels.class);

  public static final int DETAILS_PANEL = 0;
  public static final int INSTALL_PANEL = 1;

  private final ApplicationsRootNode rootNode;
  private final Project project;
  private ChartVersions chart;
  private final Disposable disposable = Disposer.newDisposable();

  public ChartPanels(ApplicationsRootNode rootNode, Disposable parentDisposable, Project project) {
    this.rootNode = rootNode;
    this.project = project;
    Disposer.register(parentDisposable, disposable);
  }

  @Override
  protected JComponent create(Integer key) {
    if (key == DETAILS_PANEL) {
      return new DetailsPanel(chart, this);
    } else if (key == INSTALL_PANEL) {
      return new InstallPanel(chart, rootNode, this, project);
    } else {
      return null;
    }
  }

  public void setChart(ChartVersions chart) {
    this.chart = chart;
    select(DETAILS_PANEL, false);
  }

  @Override
  protected Integer prepare(Integer key) {
    setChartToPanel(key);
    return key;
  }

  @Override
  public void dispose() {
    disposable.dispose();
  }

  private void setChartToPanel(Integer key) {
    if (getComponents().length > key) {
      Component component = getComponent(key);
      if (component instanceof ChartPanel) {
        ((ChartPanel) component).setChart(chart);
      }
    }
  }
}
