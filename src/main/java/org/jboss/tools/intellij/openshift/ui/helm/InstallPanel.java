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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.StatusIcon;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_BACKGROUND;
import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_UI;

class InstallPanel extends JBPanel<InstallPanel> implements ChartPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChartPanels.class);

  private final ChartVersions chart;
  private final ApplicationsRootNode rootNode;
  private final MultiPanel multiPanel;
  private final Helm helm;

  private JLabel icon;
  private JTextField releaseNameText;
  private JLabel chartNameLabel;
  private ComboBox<String> versionsCombo;
  private JBTextField parameters;
  private ComponentValidator releaseNameValidator;
  private StatusIcon statusIcon;
  private JBTextArea installResultText;
  private JPanel installResultPanel;
  private JButton installButton;

  private PanelState state = PanelState.INSTALLABLE;
  private InstallResult installResult;

  InstallPanel(ChartVersions chart, ApplicationsRootNode rootNode, MultiPanel multiPanel, Helm helm) {
    super(new MigLayout(
        "flowx, fillx, hidemode 3",
        "[50:50:50] [left, 100:100:100] [left] [left, fill] [right]"),
      true);
    this.chart = chart;
    this.rootNode = rootNode;
    this.multiPanel = multiPanel;
    this.helm = helm;
    initComponents();
  }

  private void initComponents() {
    this.icon = new JBLabel();
    add(icon, "spany 2, aligny center");

    add(new JBLabel("Chart name:"), "");
    this.chartNameLabel = new JBLabel();
    SwingUtils.setBold(chartNameLabel);
    add(chartNameLabel, "gapleft 4");

    this.statusIcon = new StatusIcon();
    add(statusIcon.get(), "pushx, growx");

    this.installButton = new JButton("Install");
    installButton.addActionListener(this::onInstall);
    add(installButton, "spany 2, pushx, alignx right, aligny center, wrap");

    add(new JBLabel("Release name:"), "");
    this.releaseNameText = new JBTextField();
    this.releaseNameValidator = new ComponentValidator(multiPanel)
      .withValidator(new ReleaseNameValidator(releaseNameText))
      .installOn(releaseNameText)
      .andRegisterOnDocumentListener(releaseNameText);
    releaseNameText.addKeyListener(onKeyPressed());
    add(releaseNameText, "spanx 2, width 200:200:200, pushx, growx, wrap");

    add(new JBLabel("Version:"), "skip");
    this.versionsCombo = new ComboBox<>(new String[]{});
    versionsCombo.addItemListener(onVersionSelected());
    add(versionsCombo, "spanx 2, width 100:100:100, gap 4 0 0 0, wrap");

    add(new JBLabel("Parameters:"), "skip");
    this.parameters = new JBTextField();
    parameters.addKeyListener(onKeyPressed());
    add(parameters, "spanx 2, pushx, growx, wrap");

    JLabel paramExplanation = new JBLabel("E.g.: --set build.uri=https://example.com");
    paramExplanation.setFont(JBUI.Fonts.smallFont());
    paramExplanation.setForeground(UIUtil.getLabelDisabledForeground());
    add(paramExplanation, "skip 2, spanx 2, pushx, growx, gap 4 0 0 0, wrap");

    this.installResultText = new JBTextArea();
    installResultText.setLineWrap(true);
    this.installResultPanel = createOutputPanel(installResultText);
    installResultPanel.setVisible(false);
    add(installResultPanel, "skip, spanx, pushx, growx, gap 0 0 0 4, wrap");

    setChart(chart);
  }

  @NotNull
  private ItemListener onVersionSelected() {
    return e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setState(PanelState.INSTALLABLE);
      }
    };
  }

  private KeyListener onKeyPressed() {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        setState(PanelState.INSTALLABLE);
      }
    };
  }

  private JPanel createOutputPanel(JBTextArea installOutput) {
    JBPanel<?> panel = new JBPanel<>(new MigLayout(
      "flowy, ins 0, gap 0 0 0 0, fillx, filly, hidemode 3"));
    panel.add(new JBLabel("Installation result:"), "gapbottom 6");
    JBScrollPane scrollPane = new JBScrollPane(installOutput);
    scrollPane.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 0, 0));
    panel.add(scrollPane, "pushx, pushy, growx, growy, height 100:100");
    return panel;
  }

  private void onInstall(ActionEvent actionEvent) {
    CompletableFuture
      .runAsync(() ->
          setState(PanelState.INSTALLING)
        , EXECUTOR_UI)
      .thenApplyAsync((Void) -> {
        try {
          String result = helm.install(
            releaseNameText.getText(),
            chart.getName(),
            (String) versionsCombo.getSelectedItem(),
            parameters.getText().trim()
          );
          return new InstallResult(result);
        } catch (IOException e) {
          LOGGER.warn("Could not install helm chart " + chart.getName() + " version " + versionsCombo.getSelectedItem() + ".", e);
          return new ErrorResult(e);
        }
      }, EXECUTOR_BACKGROUND)
      .thenAcceptAsync((InstallResult result) -> {
        this.installResult = result;
        if (result.isError()) {
          setState(PanelState.ERROR);
        } else {
          NodeUtils.fireModified(rootNode);
          setState(PanelState.INSTALLED);
        }
      }, EXECUTOR_UI);
  }

  @Override
  public void setChart(ChartVersions chart) {
    /*
     * required to invoke later because otherwise {@link IdeFocusManager} sets
     * the focus so that UI is freezing as soon as releaseNameText is cleared.
     * Culprit is {@link com.intellij.ui.CardLayoutPanel#select(ActionCallback, Object, Object)}
     * which sets the focus to components and may set invalid ones.
     */
    ApplicationManager.getApplication().invokeLater(() -> {
        updateComponents(chart);
        setState(PanelState.INSTALLABLE);
      }
    );
  }

  private void updateComponents(ChartVersions chart) {
    if (chart == null) {
      return;
    }
    icon.setIcon(ChartIcons.getIcon(chart.getName()));
    chartNameLabel.setText(chart.getName());
    releaseNameText.setText(null);
    setComboVersions(chart);
    parameters.setText(null);
  }

  private void setComboVersions(ChartVersions chart) {
    if (versionsCombo == null
      || chart == null) {
      return;
    }
    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) versionsCombo.getModel();
    model.removeAllElements();
    List<String> versions = chart.getVersions();
    model.addAll(versions);
    String latestVersion = versions.get(0);
    model.setSelectedItem(latestVersion);
  }

  private void enableInstallButton(ValidationInfo validation) {
    if (installButton != null) {
      installButton.setEnabled(validation == null);
    }
  }

  private void setState(PanelState panelState) {
    if (this.state != null
      && this.state == panelState) {
      return;
    }
    this.state = panelState;
    updateComponents(panelState);
  }

  private void updateComponents(PanelState panelState) {
    switch (panelState) {
      case INSTALLABLE:
        statusIcon.setEmpty();
        releaseNameText.setEnabled(true);
        versionsCombo.setEnabled(true);
        parameters.setEnabled(true);
        installResultText.setForeground(releaseNameText.getForeground());
        installResultText.setText(null);
        installResultPanel.setVisible(false);
        releaseNameValidator.revalidate();
        enableInstallButton(releaseNameValidator.getValidationInfo());
        break;
      case INSTALLED:
        statusIcon.setSuccess("Installed");
        releaseNameText.setEnabled(true);
        versionsCombo.setEnabled(true);
        parameters.setEnabled(true);
        installResultPanel.setVisible(true);
        installResultText.setForeground(releaseNameText.getForeground());
        installResultText.setText(installResult.getMessage());
        installButton.setEnabled(false);
        break;
      case INSTALLING:
        statusIcon.setLoading();
        releaseNameText.setEnabled(false);
        versionsCombo.setEnabled(false);
        parameters.setEnabled(false);
        installResultText.setText(null);
        installButton.setEnabled(false);
        break;
      case ERROR:
        statusIcon.setError("Installation failed");
        releaseNameText.setEnabled(true);
        versionsCombo.setEnabled(true);
        parameters.setEnabled(true);
        installResultPanel.setVisible(true);
        installResultText.setForeground(SwingUtils.getErrorForeground());
        installResultText.setText(installResult.getMessage());
        installButton.setEnabled(false);
        break;
    }
  }

  private class ReleaseNameValidator implements Supplier<ValidationInfo> {

    private final Pattern nameRegex = Pattern.compile(
      "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");
    private final JTextField field;

    public ReleaseNameValidator(JTextField field) {
      this.field = field;
    }

    @Override
    public ValidationInfo get() {
      if (!field.isEnabled()
        || !field.isVisible()) {
        return null;
      }
      ValidationInfo validation = validate(field.getText());
      enableInstallButton(validation);
      return validation;
    }

    private ValidationInfo validate(String name) {
      ValidationInfo validation;
      if (StringUtil.isEmptyOrSpaces(name)) {
        validation = new ValidationInfo("Provide a name", field).asWarning();
      } else if (name.length() > 53) {
        validation = new ValidationInfo("Must be shorter than 53 characters", field);
      } else if (!nameRegex.matcher(name).matches()) {
        validation = new ValidationInfo("Must be alphanumeric and may contain . or -", field);
      } else {
        validation = null;
      }
      return validation;
    }
  }

  private enum PanelState {
    INSTALLABLE, INSTALLING, INSTALLED, ERROR
  }

  private static class InstallResult {
    private final String message;

    protected InstallResult(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    public boolean isError() {
      return false;
    }
  }

  private static class ErrorResult extends InstallPanel.InstallResult {
    private ErrorResult(Exception e) {
      super(e.getMessage());
    }

    @Override
    public boolean isError() {
      return true;
    }
  }
}