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
package org.jboss.tools.intellij.openshift.ui.helm;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.validation.DialogValidation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.redhat.devtools.intellij.common.ui.UndecoratedDialog;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import net.miginfocom.swing.MigLayout;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoriesNode;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.tools.intellij.openshift.ui.SwingUtils.setBold;

public class AddHelmRepoDialog extends UndecoratedDialog {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddHelmRepoDialog.class);

  protected final Collection<HelmRepository> existingRepositories;
  protected final HelmRepositoriesNode repositoriesNode;
  protected final Helm helm;
  protected final Project project;
  private final Point location;
  protected JBLabel title;
  protected JBTextField nameText;
  protected JBTextField urlText;
  protected JBTextField flagsText;
  private boolean canGenerateName = true;

  public AddHelmRepoDialog(Collection<HelmRepository> repositories, HelmRepositoriesNode repositoriesNode, Helm helm, Project project, Point location) {
    super(project, null, false, IdeModalityType.MODELESS, true);
    this.existingRepositories = repositories;
    this.repositoriesNode = repositoriesNode;
    this.helm = helm;
    this.project = project;
    this.location = location;
    init();
  }

  @Override
  protected void init() {
    super.init();

    setOKButtonText("Add");
    setGlassPaneResizable();
    setMovableUsing(title);
    if (location != null) {
      setLocation(location);
    }
    registerEscapeShortcut(e -> closeImmediately());
    IdeFocusManager.getInstance(project).requestFocus(urlText, true);
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    DialogPanel panel = new DialogPanel(new MigLayout(
      "flowx, ins 4, gap 4, fillx, filly, hidemode 3",
      "[left][300:pref][right]"));
    Map<JComponent, List<DialogValidation>> validators = new HashMap<>();
    panel.setValidationsOnInput(validators);
    panel.setValidationsOnApply(validators);

    this.title = new JBLabel("Add repository");
    setBold(title);
    panel.add(title, "spanx 2, gap 0 0 0 14");

    JBLabel closeIcon = new JBLabel();
    closeIcon.setIcon(AllIcons.Windows.CloseSmall);
    closeIcon.addMouseListener(onClose());
    panel.add(closeIcon, "aligny top, wrap");

    JBLabel nameLabel = new JBLabel("Name:");
    panel.add(nameLabel);
    this.nameText = new JBTextField();
    panel.add(nameText, "growx, spanx 2, wrap");
    validators.put(nameText, Collections.singletonList(validateName(nameText)));
    nameText.addKeyListener(onKeyInName());

    JBLabel urlLabel = new JBLabel("URL:");
    panel.add(urlLabel);
    this.urlText = new JBTextField();
    panel.add(urlText, "growx, spanx 2, wrap");
    validators.put(urlText, Collections.singletonList(validateURL(urlText)));
    urlText.addKeyListener(onKeyInUrl(nameText, urlText));

    JBLabel flagsLabel = new JBLabel("Flags:");
    panel.add(flagsLabel);
    this.flagsText = new JBTextField();
    panel.add(flagsText, "growx, spanx 2, wrap");
    flagsText.addKeyListener(onKeyInFlags());

    initValidation();
    panel.validateAll();
    return panel;
  }

  private KeyListener onKeyInUrl(JBTextField nameText, JBTextField urlTextField) {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        String host = urlTextField.getText();
        if (!StringUtil.isEmptyOrSpaces(host)
          && canGenerateName()) {
          nameText.setText(getName(host));
          setCanGenerateName(true);
        }
        doOkOnEnterKey(e);
        super.keyReleased(e);
      }
    };
  }

  private KeyListener onKeyInName() {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        setCanGenerateName(false);
        doOkOnEnterKey(e);
      }
    };
  }

  private KeyListener onKeyInFlags() {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        doOkOnEnterKey(e);
      }
    };
  }

  private void doOkOnEnterKey(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      doOKAction();
    }
  }

  @NotNull
  protected DialogValidation validateName(JBTextField textField) {
    return () -> {
      String name = textField.getText();
      if (StringUtil.isEmptyOrSpaces(name)) {
        return new ValidationInfo("Name required", textField);
      } else if (name.contains("/")) {
        return new ValidationInfo("Name must not contain '/'", textField);
      } else if (existingRepositories.stream().anyMatch(
        repository -> repository.getName().equals(name))) {
        return new ValidationInfo("Repository with this name already exists", textField);
      }
      return null;
    };
  }

  @NotNull
  protected DialogValidation validateURL(JBTextField textField) {
    return () -> {
      String url = textField.getText();
      if (StringUtil.isEmptyOrSpaces(url)) {
        return  new ValidationInfo("Url required", textField);
      } else {
        String host = com.redhat.devtools.intellij.common.utils.UrlUtils.getHost(url);
        if (host == null) {
          return new ValidationInfo("Invalid URL", textField);
        }
      }
      return null;
    };
  }

  private String getName(String url) {
    try {
      URI uri = URI.create(url);
      String host = uri.getHost();
      if (StringUtil.isEmptyOrSpaces(host)) {
        return null;
      }
      String path = uri.getPath();
      if (StringUtil.isEmptyOrSpaces(path)) {
        return host;
      } else {
        // "https://docs.wildfly.org/charts/wildfly-charts/ -> docs.wildfly.org-charts-wildfly-charts
        if (path.endsWith("/")) {
          path = path.substring(0, path.length() - 1);
        }
        String appendix = path
          .replaceAll("/", "-");
        return host + appendix;
      }
    } catch (IllegalArgumentException e) {
      return null;
    }

  }

  protected boolean canGenerateName() {
    return canGenerateName;
  }

  private void setCanGenerateName(boolean canGenerateName) {
    this.canGenerateName = canGenerateName;
  }

  private MouseAdapter onClose() {
    return new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        close(0);
      }
    };
  }

  @Override
  protected void doOKAction() {
    addRepo(nameText.getText(), urlText.getText(), flagsText.getText(), helm);
    super.doOKAction();
  }

  private void addRepo(String name, String url, String flags, Helm helm) {
    if (StringUtil.isEmptyOrSpaces(url)
      || StringUtil.isEmptyOrSpaces(name)) {
      return;
    }

    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Adding helm repo " + name, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          helm.addRepo(name, url, StringUtil.isEmptyOrSpaces(flags)? null : flags);
          NodeUtils.fireModified(repositoriesNode);
        } catch (IOException e) {
          NotificationUtils.notifyError("Could not add helm repo " + name, e.getMessage());
        }
      }
    });
  }

}
