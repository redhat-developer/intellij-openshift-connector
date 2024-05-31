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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.validation.DialogValidation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBTextField;
import java.awt.Point;
import java.io.IOException;
import java.util.Collection;
import org.jboss.tools.intellij.openshift.actions.NodeUtils;
import org.jboss.tools.intellij.openshift.actions.NotificationUtils;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoriesNode;
import org.jboss.tools.intellij.openshift.tree.application.HelmRepositoryNode;
import org.jboss.tools.intellij.openshift.utils.helm.Helm;
import org.jboss.tools.intellij.openshift.utils.helm.HelmRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditHelmRepoDialog extends AddHelmRepoDialog {

  private static final Logger LOGGER = LoggerFactory.getLogger(EditHelmRepoDialog.class);

  private final HelmRepositoryNode repositoryNode;

  public EditHelmRepoDialog(Collection<HelmRepository> repositories, HelmRepositoryNode repositoryNode, HelmRepositoriesNode repositoriesNode, Helm helm, Project project, Point location) {
    super(repositories, repositoriesNode, helm, project, location);
    this.repositoryNode = repositoryNode;
    setFields(repositoryNode.getRepository());
  }

  @Override
  protected void init() {
    super.init();
    title.setText("Edit Repository");
    setOKButtonText("Change");
    enableOKButton(false);
  }

  protected void setFields(HelmRepository repository) {
    nameText.setText(repository.getName());
    urlText.setText(repository.getUrl());
  }

  private boolean isRepositoryChanged() {
    HelmRepository repository = repositoryNode.getRepository();
    if (repository == null) {
      return false;
    }
    return !nameText.getText().equals(repository.getName())
      || !urlText.getText().equals(repository.getUrl());
  }

  private void enableOKButton(boolean enable) {
    myOKAction.setEnabled(enable);
  }

  @NotNull
  @Override
  protected DialogValidation validateName(JBTextField textField) {
    return () -> {
      String name = textField.getText();
      if (StringUtil.isEmptyOrSpaces(name)) {
        return new ValidationInfo("Name required", textField);
      } else if (name.contains("/")) {
        return new ValidationInfo("Name must not contain '/'", textField);
      } else if (
        !name.equals(repositoryNode.getRepository().getName())
          && existingRepositories.stream().anyMatch(repository -> repository.getName().equals(name))) {
        return new ValidationInfo("Repository with this name already exists", textField);
      }
      enableOKButton(isRepositoryChanged());
      return null;
    };
  }

  @Override
  protected @NotNull DialogValidation validateURL(JBTextField textField) {
    return () -> {
      enableOKButton(isRepositoryChanged());
      return super.validateURL(textField).validate();
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

    ProgressManager.getInstance().run(new Task.Backgroundable(project, "Editing helm repo " + name, true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        try {
          helm.removeRepos(repositoryNode.getName());
          String repoFlags =  StringUtil.isEmptyOrSpaces(flags)? null: flags;
          helm.addRepo(name, url, repoFlags);
          NodeUtils.fireModified(repositoriesNode);
        } catch (IOException e) {
          LOGGER.warn("Could not edit repo " + name, e);
          NotificationUtils.notifyError("Could not edit helm repo " + name, e.getMessage());
        }
      }
    });
  }

  @Override
  protected boolean canGenerateName() {
    return false;
  }

}
