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
package org.jboss.tools.intellij.openshift.ui.helm.install;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.ui.StatusIcon;
import org.jboss.tools.intellij.openshift.ui.SwingUtils;
import org.jboss.tools.intellij.openshift.ui.helm.ChartIcons;
import org.jboss.tools.intellij.openshift.ui.helm.ChartPanel;
import org.jboss.tools.intellij.openshift.ui.helm.ChartVersions;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmCli;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_BACKGROUND;
import static org.jboss.tools.intellij.openshift.ui.SwingUtils.EXECUTOR_UI;

public class InstallPanel extends JBPanel<InstallPanel> implements ChartPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstallPanel.class);

  private ChartVersions chart;
  private final ApplicationsRootNode rootNode;
  private final Disposable parentDisposable;
  private final Helm helm;
  private final Odo odo;
  private final Project project;

  private final TelemetryMessageBuilder.ActionMessage telemetry =
    TelemetryService.instance().getBuilder().action(TelemetryService.PREFIX_ACTION + "install helm chart");


  private PanelState state = PanelState.INSTALLABLE;
  private Result installResult;
  private boolean areValuesValid = true;
  private ValidationInfo releaseNameValidationInfo;

  private JLabel icon;
  private JTextField releaseNameText;
  private JBLabel currentProjectLabel;
  private JLabel chartNameLabel;
  private ComboBox<String> versionsComboBox;
  private final DefaultComboBoxModel<String> versionsComboBoxModel = new DefaultComboBoxModel<>();
  private ComponentValidator releaseNameValidator;
  private StatusIcon statusIcon;
  private ValuesOrResultPanel valuesOrResultPanel;
  private JButton installButton;

  public InstallPanel(ChartVersions chart, ApplicationsRootNode rootNode, Disposable parentDisposable, Helm helm, Odo odo, Project project) {
    super(new MigLayout(
        "flowx, fillx, hidemode 3",
        "[50:50:50] [left] 10 [left, fill, grow] [left, fill] [right]"),
      true);
    this.chart = chart;
    this.rootNode = rootNode;
    this.parentDisposable = parentDisposable;
    this.helm = helm;
    this.odo = odo;
    this.project = project;
    createComponents();
  }

  @Override
  public void setChart(ChartVersions chart) {
    this.chart = chart;
    /*
     * required to invoke later because otherwise {@link IdeFocusManager} sets
     * the focus so that UI is freezing as soon as releaseNameText is cleared.
     * Culprit is {@link com.intellij.ui.CardLayoutPanel#select(ActionCallback, Object, Object)}
     * which sets the focus to components and may set invalid ones.
     */
    ApplicationManager.getApplication().invokeLater(() -> {
        updateComponents(chart);
        setState(PanelState.INSTALLABLE, true);
      }
    );
  }

  private void createComponents() {
    this.icon = new JBLabel();
    add(icon, "spany 2, aligny center");

    add(new JBLabel("Chart name:"), "");
    this.chartNameLabel = new JBLabel();
    SwingUtils.setBold(chartNameLabel);
    add(chartNameLabel, "gapleft 4, growx, pushx");

    this.statusIcon = new StatusIcon();
    add(statusIcon.get(), "pushx, growx");

    this.installButton = new JButton("Install");
    installButton.addActionListener(this::onInstall);
    add(installButton, "spany 2, pushx, alignx right, aligny center, wrap");

    add(new JBLabel("Release name:"), "");
    this.releaseNameText = new JBTextField();
    this.releaseNameValidator = new ComponentValidator(parentDisposable)
      .withValidator(new ReleaseNameValidator(releaseNameText))
      .installOn(releaseNameText)
      .andRegisterOnDocumentListener(releaseNameText);
    releaseNameText.addKeyListener(onKeyPressed(installButton));
    add(releaseNameText, "spanx 2, pushx, growx, wrap");

    add(new JBLabel("Active " + (odo.isOpenShift() ? "project:" : "namespace:")), "skip, pushx");
    this.currentProjectLabel = new JBLabel();
    // value set in validation
    add(currentProjectLabel, "pushx, wrap");

    add(new JBLabel("Version:"), "skip");
    this.versionsComboBox = new ComboBox<>(versionsComboBoxModel);
    versionsComboBox.addItemListener(onVersionSelected());
    add(versionsComboBox, "spanx 2, width 100:100:100, gap 4 0 0 0, wrap");

    this.valuesOrResultPanel = new ValuesOrResultPanel(parentDisposable, project);
    add(valuesOrResultPanel, "skip, spanx, push, grow, gap 0 0 0 4, height 400:600, wrap");

    setChart(chart);
    valuesOrResultPanel.setValuesListener(onValuesChanged());
    valuesOrResultPanel.showLoading();
  }

  private void loadAndShowChartValues(String chart) {
    CompletableFuture
      .runAsync(() -> valuesOrResultPanel.showLoading())
      .thenApplyAsync((Void) -> {
          try {
            return helm.showValues(chart);
          } catch (IOException e) {
            throw new CompletionException(e);
          }
        },
        EXECUTOR_BACKGROUND)
      .thenAcceptAsync((values) ->
          valuesOrResultPanel.showChartValues(values),
        EXECUTOR_UI);
  }

  private CompletableFuture<String> loadCurrentNamespace() {
    return CompletableFuture.supplyAsync(() -> {
        try {
          HelmCli.HelmEnv env = helm.env();
          return env.get(HelmCli.HelmEnv.HELM_NAMESPACE);
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      },
      EXECUTOR_BACKGROUND);
  }

  private ItemListener onVersionSelected() {
    return e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setState(PanelState.INSTALLABLE);
      }
    };
  }

  private KeyListener onKeyPressed(JButton installButton) {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        setState(PanelState.INSTALLABLE); // switch from result panel to value editor
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          installButton.doClick();
        }
      }
    };
  }

  private void onInstall(ActionEvent actionEvent) {
    CompletableFuture
      .runAsync(() ->
        setState(PanelState.INSTALLING),
        EXECUTOR_UI)
      .thenApplyAsync((Void) ->
        installHelmChart(),
        EXECUTOR_BACKGROUND)
      .thenAcceptAsync(this::setState,
        EXECUTOR_UI);
  }

  @NotNull
  private Result installHelmChart() {
    try {
      telemetry.property("chart", chart.getName());
      File valuesFile = createValuesFile();
      String result = helm.install(
        releaseNameText.getText(),
        chart.getName(),
        (String) versionsComboBox.getSelectedItem(),
        valuesFile
      );
      return new Success(result);
    } catch (IOException e) {
      LOGGER.warn("Could not install helm chart " + chart.getName() + " version " + versionsComboBox.getSelectedItem() + ".", e);
      return new Error(e);
    }
  }

  @NotNull
  private File createValuesFile() throws IOException {
    File valuesFile = FileUtil.createTempFile(chart.getName(), null);
    FileUtil.writeToFile(valuesFile, valuesOrResultPanel.getChartValues());
    return valuesFile;
  }

  private void updateComponents(ChartVersions chart) {
    if (chart == null) {
      return;
    }
    icon.setIcon(ChartIcons.getIcon(chart));
    chartNameLabel.setText(chart.getName());
    releaseNameText.setText(null);
    setComboVersions(chart);
    loadAndShowChartValues(chart.getName());
  }

  private void setComboVersions(ChartVersions chart) {
    if (versionsComboBoxModel == null
      || chart == null) {
      return;
    }
    versionsComboBoxModel.removeAllElements();
    List<String> versions = chart.getVersions();
    if (versions != null
      && !versions.isEmpty()) {
      versionsComboBoxModel.addAll(versions);
      String latestVersion = versions.get(0);
      versionsComboBoxModel.setSelectedItem(latestVersion);
    }
  }

  private void enableInstallButton() {
    enableInstallButton(
        releaseNameValidationInfo == null
          && areValuesValid);
  }

  private void enableInstallButton(ValidationInfo validationInfo) {
    enableInstallButton(
      validationInfo == null
        && areValuesValid);
  }

  private void enableInstallButton(boolean enabled) {
    if (installButton != null) {
      installButton.setEnabled(enabled);
    }
  }

  private void setState(Result result) {
    this.installResult = result;
    if (result.isError()) {
      setState(PanelState.ERROR);
      telemetry.error(result.getMessage()).send();
    } else {
      NodeUtils.fireModified(rootNode);
      setState(PanelState.INSTALLED);
      telemetry.success().send();
    }
  }

  private void setState(PanelState panelState) {
    setState(panelState, false);
  }

  private void setState(PanelState panelState, boolean force) {
    if (!force
      && state == panelState) {
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
        versionsComboBox.setEnabled(true);
        valuesOrResultPanel.setVisible(true);
        valuesOrResultPanel.setEnabled(true);
        loadAndShowChartValues(chart.getName());
        releaseNameValidator.revalidate();
        enableInstallButton();
        updateCurrentProjectLabel();
        break;
      case INSTALLED:
        statusIcon.setSuccess("Installed");
        releaseNameText.setEnabled(true);
        versionsComboBox.setEnabled(true);
        valuesOrResultPanel.setVisible(true);
        valuesOrResultPanel.setEnabled(true);
        valuesOrResultPanel.showInstallResult(installResult.getMessage());
        enableInstallButton(false);
        break;
      case INSTALLING:
        statusIcon.setLoading();
        releaseNameText.setEnabled(false);
        versionsComboBox.setEnabled(false);
        valuesOrResultPanel.setVisible(false);
        enableInstallButton(false);
        break;
      case ERROR:
        statusIcon.setError("Installation failed");
        releaseNameText.setEnabled(true);
        versionsComboBox.setEnabled(true);
        valuesOrResultPanel.setEnabled(true);
        valuesOrResultPanel.setVisible(true);
        valuesOrResultPanel.showInstallError(installResult.getMessage());
        enableInstallButton(false);
        break;
    }
  }

  private void updateCurrentProjectLabel() {
    loadCurrentNamespace()
      .thenAcceptAsync(currentProject -> InstallPanel.this.currentProjectLabel.setText(currentProject)
        , EXECUTOR_UI);
  }

  private class ReleaseNameValidator implements Supplier<ValidationInfo> {

    private final Pattern nameRegex = Pattern.compile(
      "^[a-z0-9]+(([.-])?[a-z0-9]){0,54}$");
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
      ValidationInfo validationInfo = validate(field.getText());
      InstallPanel.this.releaseNameValidationInfo = validationInfo;
      /*
       * I enable the button within the validator because the following alternatives don't work:
       *  - listen to changes in the text field and enable/disabled based on the validator state: validator state is updated AFTER change is notified.
       *  - install component validator on chart values (yaml) editor: not possible, it only allows JTextComponent(s)
       *  - enable/disable install button upon validator state change: not possible, there's no listener for it
       */
      enableInstallButton(validationInfo);
      return validationInfo;
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

  private @NotNull DocumentListener onValuesChanged() {
    return new DocumentListener() {
      @Override
      public void documentChanged(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        String yaml = document.getText();
        InstallPanel.this.areValuesValid = isValid(yaml);
        enableInstallButton();
      }

      private boolean isValid(String yaml) {
        try {
          /*
           * psi element is valid even if invalid tokens are present.
           * There's no easy way to query schema validation. I'm thus simply trying to parse it
           */
          new Yaml().load(yaml);
          return true;
        } catch (Exception e) {
          return false;
        }
      }
    };
  }

  private enum PanelState {
    INSTALLABLE, INSTALLING, INSTALLED, ERROR
  }

  interface Result {
    String getMessage();

    boolean isError();
  }

  static class Success implements Result {
    private final String message;

    protected Success(String message) {
      this.message = message;
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public boolean isError() {
      return false;
    }
  }

  static class Error extends Success {
    Error(Exception e) {
      super(e.getMessage());
    }

    @Override
    public boolean isError() {
      return true;
    }
  }
}