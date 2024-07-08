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
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;

class DetailsPanel extends JBPanel<DetailsPanel> implements ChartPanel, Disposable {

  private final ChartVersions chart;

  private final Disposable disposable = Disposer.newDisposable();
  private final MultiPanel multiPanel;
  private JLabel iconLabel;
  private JLabel name;
  private JLabel versionsLabel;
  private JLabel descriptionLabel;
  private JButton installButton;

  DetailsPanel(ChartVersions chart, Disposable parentDisposable, MultiPanel multiPanel) {
    super(new MigLayout(
        "flowx, fillx, hidemode 3",
        "[50:50:50][100:100:100][fill][right]"),
      true);
    this.chart = chart;
    this.multiPanel = multiPanel;
    Disposer.register(parentDisposable, disposable);
    initComponents();
  }

  private void initComponents() {
    this.iconLabel = new JBLabel();
    add(iconLabel, "spany 2, aligny center");

    add(new JBLabel("Name:"), "");
    this.name = new JBLabel();
    SwingUtils.setBold(name);
    add(name, "");

    this.installButton = new JButton("Install");
    installButton.setEnabled(false); // disabled until chart is set
    installButton.addActionListener(this::onInstall);
    add(installButton, "spany 2, alignx right, wrap");

    add(new JBLabel("Versions:"), "");
    this.versionsLabel = new JBLabel();
    add(versionsLabel, "growy, wrap");

    this.descriptionLabel = new JBLabel();
    add(descriptionLabel, "skip, spanx, pushy, aligny top, gap 0 0 10 0, wrap");

    setChart(chart);
  }

  @Override
  public void setChart(ChartVersions chart) {
    updateComponents(chart);
  }

  private void updateComponents(ChartVersions chart) {
    if (chart == null) {
      return;
    }
    if (iconLabel != null) {
      this.iconLabel.setIcon(ChartIcons.getIcon(chart));
    }
    if (name != null) {
      this.name.setText(chart.getName());
    }
    if (versionsLabel != null) {
      this.versionsLabel.setText(
        SwingUtils.embedInHtml(
          String.join(", ", chart.getVersions())
        ));
    }

    if (descriptionLabel != null) {
      this.descriptionLabel.setText(
        SwingUtils.embedInHtml(
          SwingUtils.linksToAnchors(chart.getDescription())
        ));
    }
    if (installButton != null) {
      installButton.setEnabled(true);
    }
  }

  private void onInstall(ActionEvent actionEvent) {
    multiPanel.select(InstallOrDetailsPanels.INSTALL_PANEL, false);
  }

  @Override
  public void dispose() {
    disposable.dispose();
  }
}